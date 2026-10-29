package com.pcdd.sonovel.ai;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.model.AiConfig;
import com.pcdd.sonovel.utils.ChineseConverter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 校对引擎：把「确定性乱码修复」与「AI 错别字修正」组织成一条可审计的流程。
 * <p>
 * 设计原则（按优先级）：
 * <ol>
 *   <li><b>宁可不改，不可改坏</b>。AI 只提出替换对，最终文本由本引擎自己拼装，
 *       模型无法整段替换正文。</li>
 *   <li><b>先懂书，再改字</b>。背景资料（百科摘要 + 专有名词白名单 + 高频词表）先于正文进入提示词。</li>
 *   <li><b>零成本优先</b>。乱码先用确定性规则清理（不花 token），清理后剩下的才是真错别字。</li>
 *   <li><b>可追溯</b>。每一处改动都记录原字、新字与理由，写进 proofread.log 供人工复核。</li>
 * </ol>
 *
 * @author pcdd
 */
@RequiredArgsConstructor
public class ProofreadEngine {

    private final AiConfig cfg;
    private final BookGlossary glossary;
    private final Suggester suggester;

    /**
     * 进度回调（WebUI 需要把长耗时任务实时推给浏览器）。默认空实现。
     */
    public interface Listener {
        default void onChunks(int completed, int total) {
        }

        Listener NOOP = new Listener() {
        };
    }

    private Listener listener = Listener.NOOP;

    public ProofreadEngine withListener(Listener listener) {
        this.listener = listener == null ? Listener.NOOP : listener;
        return this;
    }

    /**
     * 是否真的会调用 AI（配置完整且提供了 suggester）
     */
    public boolean aiEnabled() {
        return cfg.usable() && suggester != null;
    }

    /**
     * 错别字建议来源。生产实现为 {@link LlmClient}，测试可注入假实现。
     */
    public interface Suggester {
        List<LlmClient.Replacement> suggest(String background, String text) throws Exception;
    }

    /**
     * 单条改动记录
     */
    public static class Edit {
        public final String find;
        public final String replace;
        public final String reason;
        public final String stage;

        public Edit(String find, String replace, String reason, String stage) {
            this.find = find;
            this.replace = replace;
            this.reason = reason;
            this.stage = stage;
        }

        public String describe() {
            return StrUtil.format("[{}] {} → {} {}", stage, find, replace,
                    StrUtil.isBlank(reason) ? "" : "（" + reason + "）");
        }
    }

    /**
     * 一章的校对结果
     */
    public static class Outcome {
        public String content;
        public final List<Edit> edits = new ArrayList<>();
        public final List<String> rejected = new ArrayList<>();
        public int garbledFixed;
        public boolean aiAttempted;
        public String aiError;

        public boolean changed() {
            return !edits.isEmpty();
        }
    }

    /**
     * 校对一章正文。入参 content 为渲染后的正文（可能含 HTML 标签）。
     *
     * @param title   章节标题（并入背景，帮助模型理解本章语境）
     * @param content 章节正文
     * @param langs   本书语言体系（zh-CN/zh-TW 等），影响乱码判定
     */
    public Outcome proofread(String title, String content, String... langs) {
        Outcome out = new Outcome();
        if (StrUtil.isBlank(content)) {
            out.content = content;
            return out;
        }

        String current = content;

        // 阶段一：确定性乱码修复（零 token）
        if (cfg.garbledFixEnabled()) {
            GarbledTextFixer.Result r = GarbledTextFixer.fix(current, langs);
            if (r.changed()) {
                out.garbledFixed = r.total();
                current = r.getText();
                out.edits.add(new Edit("乱码×" + r.total(), "已清理",
                        StrUtil.format("不可逆乱码删除 {} 处，mojibake 还原 {} 处，噪声 {} 处",
                                r.getIrreversibleRemoved(), r.getMojibakeFixed(), r.getNoiseRemoved()),
                        "garbled"));
            }
        }

        // 阶段二：AI 错别字修正
        if (!aiEnabled()) {
            out.content = current;
            return out;
        }

        out.aiAttempted = true;
        try {
            current = aiPass(title, current, out, langs);
        } catch (Exception e) {
            // AI 失败绝不能破坏已下载的正文
            out.aiError = e.getMessage();
        }
        out.content = current;
        return out;
    }

    /**
     * 按行片段组合成块送 AI；替换对定位到具体片段后在该片段内应用，最后原位回填 HTML。
     * <p>
     * 之所以在「片段级」而不是「整块级」应用替换：片段就是原 HTML 里的一段可见文本，
     * 原位回填时不必依赖任何偏移推算，结构绝不会被改坏。
     */
    private String aiPass(String title, String content, Outcome out, String... langs) {
        List<HtmlTextUtil.Segment> segments = HtmlTextUtil.segments(content);
        if (segments.isEmpty()) {
            return content;
        }

        // 片段序号 -> 当前文本（初始为原文，随校对逐步更新）
        Map<Integer, String> working = new LinkedHashMap<>();
        for (HtmlTextUtil.Segment s : segments) {
            working.put(s.index(), s.text());
        }

        String background = buildBackground(title, langs);
        List<List<Integer>> plan = planChunks(segments, cfg.getChunkSize());
        int done = 0;
        for (List<Integer> chunk : plan) {
            done++;
            listener.onChunks(done, plan.size());
            /**
             * 块间节流：AI 服务普遍按 RPM 限流，而整本书在一个文件时全部块会在
             * 同一个线程里背靠背发出（章级节流帮不上）。首块不等待。
             */
            if (done > 1 && cfg.getMinInterval() != null && cfg.getMinInterval() > 0) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(
                            cfg.getMinInterval(), cfg.getMinInterval() * 2L + 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            String composed = String.join("\n", chunk.stream().map(working::get).toList());
            List<LlmClient.Replacement> suggestions;
            try {
                suggestions = suggester.suggest(background, composed);
            } catch (Exception e) {
                // 单块失败不影响其余块；整体失败由上层记录
                out.rejected.add("本块请求失败：" + e.getMessage());
                continue;
            }
            for (LlmClient.Replacement s : suggestions) {
                if (!validate(s, glossary, out, langs)) continue;
                /**
                 * 只在<b>本块</b>的片段里定位 find。两个理由：
                 * ① 模型只看到本块内容，它指的必然是本块里的那一处；整文件范围搜索
                 *    会把改动落到别处同名片段上（整本书一个文件时尤其危险）。
                 * ② 成品 TXT 是一整个文件、片段数以万计，全书搜索会让复杂度爆到不可接受。
                 */
                List<Integer> hits = new ArrayList<>();
                for (Integer idx : chunk) {
                    if (countOccurrences(working.get(idx), s.find()) > 0) hits.add(idx);
                }
                if (hits.isEmpty()) {
                    reject(out, s, "本块中不存在该片段（疑似幻觉）");
                    continue;
                }
                int idx = hits.get(0);
                if (hits.size() > 1 || countOccurrences(working.get(idx), s.find()) > 1) {
                    reject(out, s, "命中 "
                            + (hits.size() > 1 ? hits.size() + " 个片段" : "同一片段内多次")
                            + "，无法唯一定位");
                    continue;
                }
                String applied = applyOne(working.get(idx), s);
                if (applied == null) {
                    reject(out, s, "定位失败");
                    continue;
                }
                working.put(idx, applied);
                out.edits.add(new Edit(s.find(), s.replace(), s.reason(), "ai"));
            }
        }

        // 回填：只替换真正被改过的片段
        Map<Integer, String> changed = new LinkedHashMap<>();
        for (HtmlTextUtil.Segment s : segments) {
            String now = working.get(s.index());
            if (!now.equals(s.text())) changed.put(s.index(), now);
        }
        return HtmlTextUtil.rebuild(content, segments, changed);
    }

    private int countOccurrences(String text, String sub) {
        int n = 0;
        int i = 0;
        while ((i = text.indexOf(sub, i)) >= 0) {
            n++;
            i += sub.length();
        }
        return n;
    }

    private String fmt(LlmClient.Replacement s, String why) {
        return StrUtil.format("{} → {} （{}）", s.find(), s.replace(), why);
    }

    /**
     * 切块：按片段顺序累加，超过 chunkSize 封块。
     * 块只是「送 AI 的上下文窗口」，替换最终落在片段上，因此切分位置不影响正确性。
     */
    private List<List<Integer>> planChunks(List<HtmlTextUtil.Segment> segments, Integer chunkSize) {
        List<List<Integer>> chunks = new ArrayList<>();
        List<Integer> cur = new ArrayList<>();
        int len = 0;
        int limit = chunkSize == null || chunkSize < 200 ? 1200 : chunkSize;

        for (HtmlTextUtil.Segment s : segments) {
            int t = s.text().length();
            if (!cur.isEmpty() && len + t + 1 > limit) {
                chunks.add(cur);
                cur = new ArrayList<>();
                len = 0;
            }
            cur.add(s.index());
            len += t + 1;
        }
        if (!cur.isEmpty()) chunks.add(cur);
        return chunks;
    }

    /**
     * 构建提示词背景块：百科摘要 + 专名白名单 + 高频词 + 本章标题
     */
    private String buildBackground(String title, String... langs) {
        StringBuilder sb = new StringBuilder(glossary.toPromptContext(900));
        if (StrUtil.isNotBlank(title)) {
            sb.append("本章标题：").append(title).append('\n');
        }
        sb.append(isTraditional(langs)
                ? "注意：本书正文为繁体中文，请保持繁体，切勿转为简体。\n"
                : "注意：本书正文为简体中文，请保持简体，切勿转为繁体。\n");
        return sb.toString();
    }

    /**
     * 替换对静态校验：任何一条不满足即拒绝并记录原因。
     * 与「能否唯一定位」分开，后者在应用阶段判断。
     *
     * @return true 表示通过
     */
    private boolean validate(LlmClient.Replacement s, BookGlossary g, Outcome out, String... langs) {
        String find = s.find();
        String replace = s.replace();

        if (StrUtil.isBlank(find) || replace == null || find.equals(replace)) {
            return reject(out, s, "空替换");
        }
        if (find.indexOf('<') >= 0 || find.indexOf('>') >= 0
                || replace.indexOf('<') >= 0 || replace.indexOf('>') >= 0) {
            return reject(out, s, "涉及 HTML 标签");
        }
        if (find.indexOf('\n') >= 0 || replace.indexOf('\n') >= 0) {
            return reject(out, s, "跨段落");
        }
        if (find.length() > 20) {
            return reject(out, s, "原文片段过长");
        }
        if (Math.abs(find.length() - replace.length()) > 2) {
            return reject(out, s, "改动幅度过大（疑似改写句式）");
        }
        if (editDistance(find, replace) > 4) {
            return reject(out, s, "差异字符过多（疑似改写句式）");
        }

        // 保护词：find 里含专有名词时，replace 必须原样保留它
        for (String term : g.getProtectedTerms()) {
            if (term.length() < 2 || !find.contains(term)) continue;
            if (!replace.contains(term)) {
                return reject(out, s, "改动了专有名词「" + term + "」");
            }
        }
        // 高频词表：find 整体是本书高频词且 replace 不在表内 -> 视为误改
        if (g.getDomainWords().contains(find) && !g.getDomainWords().contains(replace)) {
            return reject(out, s, "改动了本书高频用词「" + find + "」");
        }

        // 字形体系约束：简繁转换属于「转换环节」的职责，不是校对。
        // 采用逐字判定而非整串往返 —— 实测「练/炼/練」是多对多映射，
        // HanLP 的 s2t 与 t2s 并非严格互逆（s2t(修炼)=修煉，而 t2s(修練)=修练），
        // 整串比较会漏判；但 t2s(練)=练≠練 能准确标记「練 本身是繁体字形」。
        boolean traditionalBook = isTraditional(langs);
        if (introducesForeignScript(find, replace, traditionalBook)) {
            return reject(out, s, traditionalBook
                    ? "本书为繁体，该建议引入了简体字形"
                    : "本书为简体，该建议引入了繁体字形");
        }
        return true;
    }

    /**
     * 本书是否应按繁体对待
     */
    static boolean isTraditional(String... langs) {
        if (langs == null) return false;
        for (String l : langs) {
            if (l == null) continue;
            String s = l.toUpperCase();
            if (s.contains("TW") || s.contains("HANT") || s.contains("HK")) return true;
        }
        return false;
    }

    /**
     * 检查建议是否引入了与本书字形体系相悖的新字符。
     * 只看 replace 相对 find 的变化部分：原文里本来就有的字形属于作者，不动它。
     */
    static boolean introducesForeignScript(String find, String replace, boolean traditionalBook) {
        for (String token : changedTokens(find, replace)) {
            Boolean trad = scriptOf(token);
            if (trad == null) continue;
            // 简体书遇到繁体字 / 繁体书遇到简体字
            if (traditionalBook == !trad) return true;
        }
        return false;
    }

    /**
     * 单个汉字的字形归属：true=繁体字形，false=简体字形，null=无法判定/非汉字
     */
    static Boolean scriptOf(String token) {
        if (token.codePointCount(0, token.length()) != 1) return null;
        int cp = token.codePointAt(0);
        if (cp < 0x3400 || cp > 0x9FFF) return null;   // 非 CJK 区不判定
        String simp = ChineseConverter.toSimplified(token);
        String trad = ChineseConverter.toTraditional(token);
        if (simp == null || trad == null) return null;
        boolean changesWhenSimplified = !simp.equals(token);
        boolean changesWhenTraditionalized = !trad.equals(token);
        if (changesWhenTraditionalized && !changesWhenSimplified) return Boolean.FALSE;
        if (changesWhenSimplified && !changesWhenTraditionalized) return Boolean.TRUE;
        // 两岸同形字（人/山）或歧义映射（练/炼）都不判定，交回正常规则
        return null;
    }

    /**
     * replace 相对 find 发生变化的字符（逐位比较；长度变化时新增部分也算进来）
     */
    private static List<String> changedTokens(String find, String replace) {
        List<String> f = toChars(find);
        List<String> r = toChars(replace);
        List<String> changed = new ArrayList<>();
        int n = Math.min(f.size(), r.size());
        for (int i = 0; i < n; i++) {
            if (!f.get(i).equals(r.get(i))) changed.add(r.get(i));
        }
        for (int i = n; i < r.size(); i++) {
            changed.add(r.get(i));
        }
        return changed;
    }

    private static List<String> toChars(String s) {
        List<String> list = new ArrayList<>();
        for (int cp : s.codePoints().toArray()) {
            list.add(new String(Character.toChars(cp)));
        }
        return list;
    }

    private boolean reject(Outcome out, LlmClient.Replacement s, String why) {
        out.rejected.add(fmt(s, why));
        return false;
    }

    /**
     * 在片段内应用替换（要求在该片段内唯一命中）
     */
    private String applyOne(String host, LlmClient.Replacement s) {
        int idx = host.indexOf(s.find());
        if (idx < 0 || host.indexOf(s.find(), idx + 1) >= 0) return null;
        return host.substring(0, idx) + s.replace() + host.substring(idx + s.find().length());
    }

    /**
     * Levenshtein 距离（片段短，直接 DP）
     */
    static int editDistance(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = ca == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[m];
    }

}
