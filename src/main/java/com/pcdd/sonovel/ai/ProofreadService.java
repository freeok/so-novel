package com.pcdd.sonovel.ai;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.context.BookContext;
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

        /**
         * LogUtils 写文件日志时依赖 BookContext（下载流程由 Crawler 设置）。
         * 本地/CLI/Web 校对没有走 Crawler，不绑定的话第一句 LogUtils.info 就 NPE，
         * 整本书校对直接失败。
         * <p>
         * 只有「本方法自己绑定」时才负责清理 —— 下载流水线已经绑定过上下文，
         * 若在此 clear 会让后续的成品合并阶段失去上下文而崩掉。
         */
        boolean ownedContext = BookContext.get() == null;
        if (ownedContext) {
            bindContext(bookName, author);
        }
        try {
            return doProofread(files, bookName, author, intro, logDir, backupBeforeWrite, progress);
        } finally {
            if (ownedContext) {
                BookContext.clear();
            }
        }
    }

    /**
     * 为日志与审计文件名绑定书籍上下文。
     * 包内可见以便单测直接覆盖「校对路径必须自带上下文」这一约束。
     */
    void bindContext(String bookName, String author) {
        Book book = new Book();
        book.setBookName(bookName);
        book.setAuthor(StrUtil.nullToEmpty(author));
        BookContext.set(book);
    }

    private String doProofread(List<File> files, String bookName, String author, String intro,
                               File logDir, boolean backupBeforeWrite, Progress progress) {
        AiConfig ai = config.getAi();

        BookGlossary glossary;
        ProofreadEngine engine;
        /**
         * 只有关联了可用的 AI 服务才值得做背景调研：调研产出的专有名词只用于
         * 「拒绝 AI 的错误建议」，AI 不调用时毫无作用，却仍会发若干网络请求
         * 并做一次全文 HanLP 分词（整本书可达数十万字，纯浪费）。
         */
        if (ai.usable()) {
            Console.log(render("<== 正在调研《{}》的背景资料（百科 + 正文专名统计）…", "cyan"), bookName);
            glossary = GlossaryCollector.collect(bookName, author, intro,
                    buildSample(files), ai.getMaxTerms());
            engine = new ProofreadEngine(ai, glossary, LlmClient.of(ai));
        } else {
            Console.log(render("<== {}：未配置可用 AI 服务，仅做确定性乱码修复", "yellow"), "AI 校对");
            glossary = new BookGlossary();
            glossary.setBookName(bookName);
            glossary.setAuthor(author);
            engine = new ProofreadEngine(ai, glossary, null);
        }

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
        AtomicInteger failedChunks = new AtomicInteger();
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
                    failedChunks.addAndGet(r.outcome.chunkFailures);
                });
            }
        }

        String summary = StrUtil.format(
                "AI 校对完成：改动 {} 章 / {} 处（乱码清理 {} 处，拒绝可疑建议 {} 条）",
                changed.get(), edits.get(), garbled.get(), rejected.get());
        /**
         * 请求失败必须单独喊出来：这些块根本没拿到建议，等于没校对过，
         * 用户需要的是重试而不是以为「都检查过了、没问题」。
         */
        if (failedChunks.get() > 0) {
            summary += StrUtil.format("；注意：{} 个送审块请求失败，未校对到内容，建议重试",
                    failedChunks.get());
        }

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
            if (!changed && out.chunkFailures == 0 && out.rejected.isEmpty()) {
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
        if (out.chunkFailures > 0) {
            sb.append("  [请求失败] ").append(out.chunkFailures)
                    .append(" 个送审块未拿到建议（").append(StrUtil.blankToDefault(out.aiError, "原因未知"))
                    .append("）；这部分内容等于没校对，需要重试\n");
        }
        // 整文件级异常（读文件、写回等）不走块计数，必须单独留痕，否则错误会被静默吞掉
        if (out.chunkFailures == 0 && StrUtil.isNotBlank(out.aiError)) {
            sb.append("  [校对异常] ").append(out.aiError).append("（正文保持原样）\n");
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
                ai.usable()
                        ? (g.isResearched() ? g.getSourceNote() : "无百科条目（保守模式）")
                        : "未配置可用 AI 服务（跳过调研）",
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
