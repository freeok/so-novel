package com.pcdd.sonovel.ai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * HTML 正文与纯文本之间的双向无损切分工具。
 * <p>
 * AI 校对只能作用于<b>纯文本</b>（把 HTML 喂给模型极易被它「顺手」改写标签结构），
 * 因此需要先抽出文本段、校对完再按原序回填。这里用占位符方案而非 DOM 重建树，
 * 目的是让标签、属性、缩进与原字节级一致，只有段内汉字发生变化。
 *
 * @author pcdd
 */
@UtilityClass
public class HtmlTextUtil {

    /**
     * 段落切分下限：短于该长度的片段不送 AI（无上下文，改错概率高于改对概率）
     */
    private final int MIN_SEGMENT = 8;

    /**
     * 抽取纯文本（用于背景调研样本拼接、专名统计）
     */
    public String text(String htmlOrText) {
        if (StrUtil.isBlank(htmlOrText)) return "";
        if (htmlOrText.indexOf('<') < 0) return htmlOrText;
        return HtmlUtil.cleanHtmlTag(htmlOrText).replace("&nbsp;", " ");
    }

    /**
     * 解析出的文本片段。start/end 为该片段在<b>原始串</b>中的字节位置，
     * 校对后可据此原位回填，标签与结构保持不动。
     */
    public record Segment(String text, int start, int end, int index) {
    }

    /**
     * 扫描 HTML，抽出所有可校对的可见文本片段（跳过标签、script/style 内容、
     * 空白片段与过短片段）。这是 segments 与 rebuild 唯一的切分依据，避免双重扫描错位。
     * <p>
     * 片段以「行」为粒度：含换行的文本块会被逐行拆开，且每片只包含行内正文本身，
     * 不含前后空白缩进（TXT 的首行缩进、模板的换行都在片段范围之外，回填后原样保留）。
     * <p>
     * 行切分在<b>原始串</b>上进行，只把选中区间解码后作为 Segment.text ——
     * start/end 永远是原串坐标，rebuild 才能安全回填。若先整块解码再按下标算坐标，
     * 实体（&ldquo; 占 7 字符、解码后 1 字符）会让解码坐标 ≠ 原串坐标，回填时错位损坏数据。
     * （本类只解码固定几个命名实体，均不含换行，因此原串与解码后的行结构一一对应。）
     */
    public List<Segment> segments(String html) {
        List<Segment> out = new ArrayList<>();
        if (StrUtil.isBlank(html)) return out;

        List<int[]> blocks = scanTextBlocks(html);

        int idx = 0;
        for (int[] block : blocks) {
            String rawBlock = html.substring(block[0], block[1]);
            // 行切分：定位每行正文（去掉首尾空白）在<原串>中的区间
            int lineCursor = 0;
            while (lineCursor < rawBlock.length()) {
                int nl = rawBlock.indexOf('\n', lineCursor);
                int lineEnd = nl < 0 ? rawBlock.length() : nl;
                String line = rawBlock.substring(lineCursor, lineEnd);
                int lead = 0;
                while (lead < line.length() && isBlankChar(line.charAt(lead))) lead++;
                int trail = line.length();
                while (trail > lead && isBlankChar(line.charAt(trail - 1))) trail--;
                if (trail > lead) {
                    int start = block[0] + lineCursor + lead;
                    int end = block[0] + lineCursor + trail;
                    // 只有这一段需要解码，坐标永远是原串的
                    String body = decode(html.substring(start, end));
                    if (body.length() >= MIN_SEGMENT) {
                        out.add(new Segment(body, start, end, idx++));
                    }
                }
                lineCursor = lineEnd + 1;
            }
        }
        return out;
    }

    /**
     * 扫出所有「标签之外」的文本块区间（跳过 script/style 内容）
     */
    private List<int[]> scanTextBlocks(String html) {
        List<int[]> blocks = new ArrayList<>();
        int i = 0;
        while (i < html.length()) {
            if (html.charAt(i) == '<') {
                int close = html.indexOf('>', i);
                if (close < 0) break;
                String tag = html.substring(i, close + 1).toLowerCase();
                if (tag.startsWith("<script") || tag.startsWith("<style")) {
                    String closing = tag.startsWith("<script") ? "</script>" : "</style>";
                    int end = html.indexOf(closing, close);
                    i = end < 0 ? html.length() : end + closing.length();
                    continue;
                }
                i = close + 1;
                continue;
            }
            int next = html.indexOf('<', i);
            int segEnd = next < 0 ? html.length() : next;
            blocks.add(new int[]{i, segEnd});
            i = segEnd;
        }
        return blocks;
    }

    /**
     * 全角空格、半角空格、制表符等均视为空白（不参与校对，也不被改动）
     */
    private boolean isBlankChar(char c) {
        return c == ' ' || c == '\t' || c == '\u3000' || c == '\u00A0' || c == '\u2007' || c == '\u202F';
    }

    /**
     * 按片段序号把校对后的文本回填进原 HTML。
     * 未在 map 中出现的片段保持原样，因此未改动处与原文完全一致。
     *
     * @param html        原始串
     * @param segments    segments(html) 的结果
     * @param proofread   片段 index -> 校对后文本
     */
    public String rebuild(String html, List<Segment> segments, java.util.Map<Integer, String> proofread) {
        if (proofread == null || proofread.isEmpty()) return html;
        StringBuilder sb = new StringBuilder(html.length());
        int cursor = 0;
        for (Segment s : segments) {
            String replacement = proofread.get(s.index());
            if (replacement == null || replacement.equals(s.text())) continue;
            /**
             * segments 来自同一次扫描时天然有序不重叠；但调用方若传入过期的片段表
             * （例如先做过一次乱码修复却拿旧片段表来回填），坐标就不可信。
             * 校验方式：把当前原串的该区间解码后应当正好等于 Segment.text —— 对不上
             * 说明坐标系不属于这个串，跳过它，与其把正文覆盖到错误位置，不如保持原样。
             * （「宁可不改，不可改坏」）
             */
            if (s.start() < cursor || s.end() > html.length() || s.start() > s.end()) {
                continue;
            }
            if (!decode(html.substring(s.start(), s.end())).equals(s.text())) {
                continue;
            }
            sb.append(html, cursor, s.start()).append(replacement);
            cursor = s.end();
        }
        sb.append(html.substring(cursor));
        return sb.toString();
    }

    /**
     * 解码实体但保留空白结构（\n 需保留以维持段落）
     */
    private String decode(String htmlChunk) {
        return htmlChunk
                .replace("&nbsp;", " ")
                .replace("&ldquo;", "“").replace("&rdquo;", "”")
                .replace("&lsquo;", "‘").replace("&rsquo;", "’")
                .replace("&mdash;", "—").replace("&hellip;", "…")
                .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"");
    }

}
