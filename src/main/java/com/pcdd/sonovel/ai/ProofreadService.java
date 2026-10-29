package com.pcdd.sonovel.ai;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.model.AiConfig;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule.Book;
import com.pcdd.sonovel.utils.FileUtils;
import com.pcdd.sonovel.utils.LogUtils;
import com.pcdd.sonovel.utils.VirtualThreadLimiter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * AI 校对调度器：对一组章节文件执行「先理解、后修改」的校对。
 * <p>
 * 三个阶段：
 * <ol>
 *   <li><b>背景调研</b>：用书名/作者/简介 + 正文样本去公开百科查这本书在讲什么，
 *       并统计本书专名，合成专有名词白名单与高频词表（{@link GlossaryCollector}）。
 *       这一步决定了「萧炎/王林/克莱恩」会不会被当成错别字改掉。</li>
 *   <li><b>逐章校对</b>：确定性乱码修复优先（零 token），再由 AI 提最小替换对，
 *       引擎逐条校验后应用（{@link ProofreadEngine}）。</li>
 *   <li><b>审计落盘</b>：每处改动与被拒绝的建议写进 校对记录.log，供人工复核。</li>
 * </ol>
 * 两个使用场景共用本类：
 * <ul>
 *   <li>下载流程内置（{@code run(book, dir)}）：在合并成品之前校对章节缓存，
 *       txt/epub/pdf/html 自动带上校对结果。</li>
 *   <li>本地已有书籍（{@code proofreadFiles(...)}）：直接校对成品文件，写回前自动备份。</li>
 * </ul>
 *
 * @author pcdd
 */
@RequiredArgsConstructor
public class ProofreadService {

    private final AppConfig config;

    /**
     * 用于背景调研的采样章节数
     */
    private static final int SAMPLE_CHAPTERS = 5;
    /**
     * 采样正文总量上限（字符），控制分词耗时与提示词预算
     */
    private static final int SAMPLE_CHARS = 30_000;

    /**
     * 该功能是否开启
     */
    public boolean enabled() {
        return config.getAi() != null && Integer.valueOf(1).equals(config.getAi().getEnabled());
    }

    /**
     * 校对下载缓存目录里的章节（就地覆盖，随后由 PostHandler 合并成品）
     *
     * @return 统计摘要，未启用时返回 null
     */
    public String run(Book book, File dir) {
        if (!enabled()) return null;

        List<File> files = collectChapterFiles(dir);
        if (files.isEmpty()) {
            LogUtils.info("AI 校对跳过：章节目录 {} 内没有可处理的文件", dir);
            return null;
        }

        return proofreadFiles(files, book.getBookName(), book.getAuthor(), book.getIntro(),
                dir.getParentFile(), false);
    }

    /**
     * 校对进度。WebUI 用它把长任务实时推给浏览器。
     * <p>
     * 两个粒度：<b>章级</b>用于「一个文件一章」的章节目录模式；
     * <b>段级</b>用于「整本书一个文件」的成品模式 —— 后者若只报章级会长时间停在 0/1。
     */
    public interface Progress {
        default void onChapterDone(int completed, int total) {
        }

        /**
         * 单个文件内部的送审块进度
         *
         * @param fileName 当前文件
         */
        default void onChunkProgress(String fileName, int completed, int total) {
        }

        Progress NOOP = new Progress() {
        };
    }

    public String proofreadFiles(List<File> files, String bookName, String author, String intro,
                                 File logDir, boolean backupBeforeWrite) {
        return proofreadFiles(files, bookName, author, intro, logDir, backupBeforeWrite, Progress.NOOP);
    }

    /**
     * 校对一组文件。
     *
     * @param files              待校对文件（TXT/HTML）
     * @param bookName           书名（决定百科检索命中率）
     * @param author             作者
     * @param intro              简介（无百科条目时的降级背景）
     * @param logDir             校对记录输出目录
     * @param backupBeforeWrite  写回前是否备份原文件（本地成品模式需要，章节缓存模式不需要）
     * @param progress           章级进度回调
     * @return 统计摘要
     */
    public String proofreadFiles(List<File> files, String bookName, String author, String intro,
                                 File logDir, boolean backupBeforeWrite, Progress progress) {
        if (files.isEmpty()) {
            return "没有可校对的文件";
        }

        AiConfig ai = config.getAi();
        Console.log(render("<== 正在调研《{}》的背景资料（百科 + 正文专名统计）…", "cyan"), bookName);

        // 1) 背景调研
        BookGlossary glossary = GlossaryCollector.collect(bookName, author, intro,
                buildSample(files), ai.getMaxTerms());

        ProofreadEngine engine = new ProofreadEngine(ai, glossary,
                ai.usable() ? LlmClient.of(ai) : null);
        // 整本书一个文件时，章级进度无意义（永远 0/1），改用文件内部的块进度
        if (files.size() == 1) {
            String only = files.get(0).getName();
            engine.withListener(new ProofreadEngine.Listener() {
                @Override
                public void onChunks(int completed, int total) {
                    progress.onChunkProgress(only, completed, total);
                }
            });
        }
        String[] langs = {StrUtil.blankToDefault(config.getLanguage(), "zh-CN")};

        // 2) 逐章校对（虚拟线程并发；词表只读共享，OkHttp 线程安全）
        int maxConcurrent = Math.min(Math.max(1, ai.getConcurrency()), files.size());
        String[] auditSlots = new String[files.size()];
        AtomicInteger changed = new AtomicInteger();
        AtomicInteger edits = new AtomicInteger();
        AtomicInteger garbled = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger aiFailed = new AtomicInteger();
        AtomicInteger done = new AtomicInteger();

        try (var limiter = new VirtualThreadLimiter(maxConcurrent)) {
            for (int i = 0; i < files.size(); i++) {
                final int index = i;
                final File f = files.get(i);
                limiter.submit(() -> {
                    Result r;
                    try {
                        r = proofreadOne(engine, f, langs, backupBeforeWrite);
                    } finally {
                        // 无论该章是否改动，都算「已处理」，否则进度条会停在半路
                        progress.onChapterDone(done.incrementAndGet(), files.size());
                    }
                    auditSlots[index] = auditOf(f, r);
                    if (r == null) return;
                    if (r.written) changed.incrementAndGet();
                    edits.addAndGet(r.outcome.edits.size());
                    garbled.addAndGet(r.outcome.garbledFixed);
                    rejected.addAndGet(r.outcome.rejected.size());
                    if (r.outcome.aiError != null) aiFailed.incrementAndGet();
                });
            }
        }

        String summary = StrUtil.format(
                "AI 校对完成：改动 {} 章 / {} 处（乱码清理 {} 处，拒绝可疑建议 {} 条，AI 请求失败 {} 章）",
                changed.get(), edits.get(), garbled.get(), rejected.get(), aiFailed.get());

        // 3) 审计落盘
        if (Integer.valueOf(1).equals(ai.getAudit()) && logDir != null) {
            String path = writeAudit(logDir, bookName, author, glossary, ai, maxConcurrent, auditSlots);
            if (path != null) {
                Console.log(render("<== 校对记录已保存：{}", "cyan"), path);
                summary = summary + "；校对记录 " + path;
            }
        }

        Console.log(render("<== {}", "green"), summary);
        return summary;
    }

    /**
     * 单文件校对结果
     */
    private static class Result {
        final ProofreadEngine.Outcome outcome;
        final String name;
        /**
         * 是否真的写回了文件（有改动才写）
         */
        final boolean written;

        Result(ProofreadEngine.Outcome outcome, String name, boolean written) {
            this.outcome = outcome;
            this.name = name;
            this.written = written;
        }
    }

    /**
     * 读文件 -> 校对 -> 写回。任何异常都退化为「保留原文」，绝不让校对破坏已下载内容。
     */
    private Result proofreadOne(ProofreadEngine engine, File f, String[] langs, boolean backup) {
        try {
            String raw = FileUtil.readString(f, StandardCharsets.UTF_8);
            if (StrUtil.isBlank(raw)) return null;

            // 零成本预判：无乱码特征且 AI 不可用则跳过，省 IO 与 token
            boolean garbled = GarbledTextFixer.looksGarbled(raw);
            if (!engine.aiEnabled() && !garbled) return null;

            // 节流由引擎按「每次 AI 请求」处理（整本书一个文件时章级节流无效）
            ProofreadEngine.Outcome out = engine.proofread(titleOf(f), raw, langs);
            boolean changed = !out.content.equals(raw);
            if (!changed && out.aiError == null && out.rejected.isEmpty()) {
                // 什么都没发生，无需进审计
                return null;
            }

            if (changed) {
                if (backup) {
                    String bakPath = f.getAbsolutePath() + ".proofread.bak";
                    if (!FileUtil.exist(bakPath)) {
                        FileUtil.copy(f, new File(bakPath), true);
                    }
                }
                FileUtil.writeString(out.content, f, StandardCharsets.UTF_8);
            }
            // 即使正文没变，也要把 AI 失败/被拒绝的建议带进审计与统计
            return new Result(out, f.getName(), changed);
        } catch (Exception e) {
            LogUtils.warn("章节 {} 校对异常，保留原文：{}", f.getName(), e.getMessage());
            ProofreadEngine.Outcome out = new ProofreadEngine.Outcome();
            out.content = null;
            out.aiError = e.getMessage();
            return new Result(out, f.getName(), false);
        }
    }

    private String auditOf(File f, Result r) {
        if (r == null) return null;
        ProofreadEngine.Outcome out = r.outcome;
        StringBuilder sb = new StringBuilder("\n### ").append(f.getName()).append('\n');
        for (ProofreadEngine.Edit e : out.edits) {
            sb.append("  ").append(e.describe()).append('\n');
        }
        for (String x : out.rejected) {
            sb.append("  [拒绝] ").append(x).append('\n');
        }
        if (out.aiError != null) {
            sb.append("  [AI 请求失败] ").append(out.aiError);
        }
        return sb.toString();
    }

    private String writeAudit(File logDir, String bookName, String author, BookGlossary g,
                              AiConfig ai, int concurrency, String[] slots) {
        List<String> lines = new ArrayList<>();
        lines.add(StrUtil.format("""
                        《{}》({}) 校对记录
                        生成时间：{}
                        背景来源：{}
                        专有名词保护：{} 个；高频词：{} 个
                        AI 服务：{}
                        并发：{}
                        说明：[拒绝] 为引擎判定不安全而放弃的 AI 建议，正文保持原样；\
                        如需复核可对比 .proofread.bak 备份。
                        {}""",
                StrUtil.blankToDefault(bookName, "未知书名"), StrUtil.blankToDefault(author, "佚名"),
                java.time.LocalDateTime.now().withNano(0),
                g.isResearched() ? g.getSourceNote() : "无百科条目（保守模式）",
                g.getProtectedTerms().size(), g.getDomainWords().size(),
                ai.usable() ? ai.getBaseUrl() + " / " + ai.getModel() : "未配置（仅确定性乱码修复）",
                concurrency,
                "-".repeat(80)));
        for (String s : slots) {
            if (s != null) lines.add(s);
        }
        String path = FileUtils.toAbsolutePath(logDir + File.separator
                + StrUtil.format("{} ({}) 校对记录.log",
                StrUtil.blankToDefault(bookName, "本书"), StrUtil.blankToDefault(author, "佚名")));
        try {
            FileUtil.writeUtf8Lines(lines, path);
            return path;
        } catch (Exception e) {
            LogUtils.warn("校对记录写入失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 章节目录里的正文文件（排除封面 0_封面.* 等附属文件）
     */
    private List<File> collectChapterFiles(File dir) {
        List<File> list = new ArrayList<>();
        for (File f : FileUtils.sortFilesByName(dir)) {
            if (!f.isFile()) continue;
            String name = f.getName();
            if (name.startsWith("0_") || name.contains("封面")) continue;
            String ext = FileUtil.extName(name).toLowerCase();
            if (ext.equals("html") || ext.equals("txt")) {
                list.add(f);
            }
        }
        return list;
    }

    /**
     * 取前几章拼样本：够百科检索与分词统计用，又不至于把整本书读进内存
     */
    private String buildSample(List<File> files) {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(SAMPLE_CHAPTERS, files.size());
        for (int i = 0; i < limit; i++) {
            try {
                sb.append(HtmlTextUtil.text(FileUtil.readString(files.get(i), StandardCharsets.UTF_8))).append('\n');
            } catch (Exception e) {
                // 采样失败只影响背景质量，不中断流程
            }
            if (sb.length() >= SAMPLE_CHARS) break;
        }
        return sb.length() > SAMPLE_CHARS ? sb.substring(0, SAMPLE_CHARS) : sb.toString();
    }

    /**
     * 从文件名取章节标题（约定 order_标题.ext）
     */
    private String titleOf(File f) {
        String name = FileUtil.mainName(f.getName());
        int idx = name.indexOf('_');
        return idx >= 0 ? name.substring(idx + 1) : name;
    }

}
