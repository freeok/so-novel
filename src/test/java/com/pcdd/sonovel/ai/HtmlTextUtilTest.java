package com.pcdd.sonovel.ai;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTML 文本切分/回填的坐标正确性测试（纯单元，不联网）。
 * <p>
 * 这里的核心风险是「错位」：片段坐标若在解码后的空间里计算、却被当作原串坐标回填，
 * 实体（&ldquo; 7 字符 → 1 字符）就会让两者长度不一致，把标签或别的正文覆盖掉。
 * 因此断言的重点不是「改对了没」，而是「没改的地方必须一个字节都不动」。
 */
class HtmlTextUtilTest {

    /**
     * segments 的坐标必须精确指向原串中对应的可见正文
     */
    @Test
    void segmentOffsetsPointAtRawText() {
        String html = "<p>第一段正文内容在这里</p>\n<p>第二段正文内容在这里</p>";
        List<HtmlTextUtil.Segment> segs = HtmlTextUtil.segments(html);

        assertEquals(2, segs.size());
        for (HtmlTextUtil.Segment s : segs) {
            assertEquals(s.text(), html.substring(s.start(), s.end()),
                    "start/end 必须是原串坐标");
        }
        assertEquals("第一段正文内容在这里", segs.get(0).text());
        assertEquals("第二段正文内容在这里", segs.get(1).text());
    }

    /**
     * 关键回归：正文里有实体时，坐标仍要落在原串上，回填不得错位。
     * 旧实现先整块解码再按下标算坐标，这里会把 </p> 甚至后一段吃掉。
     */
    @Test
    void entitiesDoNotShiftOffsets() {
        String html = "<p>他说&ldquo;你好啊朋友&rdquo;然后离开</p><p>第二段正文内容保持不动</p>";
        List<HtmlTextUtil.Segment> segs = HtmlTextUtil.segments(html);

        assertEquals(2, segs.size());
        // 解码后的文本给模型看
        assertEquals("他说“你好啊朋友”然后离开", segs.get(0).text());
        // 但坐标指向原串（含实体原文）
        assertEquals(html.substring(segs.get(0).start(), segs.get(0).end()),
                segs.get(0).text().replace("“", "&ldquo;").replace("”", "&rdquo;"));

        Map<Integer, String> fix = new HashMap<>();
        fix.put(0, "他说&ldquo;你好呀朋友&rdquo;然后离开");
        String out = HtmlTextUtil.rebuild(html, segs, fix);

        assertEquals("<p>他说&ldquo;你好呀朋友&rdquo;然后离开</p><p>第二段正文内容保持不动</p>", out);
    }

    /**
     * 未出现在 map 里的片段必须逐字节保持原样
     */
    @Test
    void rebuildLeavesUntouchedSegmentsByteIdentical() {
        String html = "<div class=\"x\">\n　　第一行正文内容在这里\n　　第二行正文内容在这里\n</div>";
        List<HtmlTextUtil.Segment> segs = HtmlTextUtil.segments(html);
        assertEquals(2, segs.size());

        Map<Integer, String> fix = new HashMap<>();
        fix.put(1, "第二行的正文内容在这里");   // 只改第二行
        String out = HtmlTextUtil.rebuild(html, segs, fix);

        assertTrue(out.contains("第一行正文内容在这里"), out);
        assertTrue(out.contains("第二行的正文内容在这里"), out);
        // 缩进与标签结构原封不动
        assertTrue(out.startsWith("<div class=\"x\">\n　　"), out);
        assertTrue(out.endsWith("\n</div>"), out);
        assertEquals(html.split("\n").length, out.split("\n").length, "行数不得变化");
    }

    /**
     * TXT 成品：全角空格缩进不参与校对（不会出现在片段里），因此不可能被改动或吃掉
     */
    @Test
    void indentIsNeverAProofreadTarget() {
        String txt = "第一章 标题\n\n　　他看着眼前的大海，久久无言。\n";
        List<HtmlTextUtil.Segment> segs = HtmlTextUtil.segments(txt);

        assertEquals(1, segs.size());
        HtmlTextUtil.Segment s = segs.get(0);
        assertEquals("他看着眼前的大海，久久无言。", s.text());
        assertFalse(s.text().startsWith("　"), "缩进不属于片段内容");
        assertEquals(txt.substring(s.start(), s.end()), s.text());
    }

    /**
     * script/style 内容绝不外泄给 AI（既非正文，也常被模型「顺手修好」而破坏结构）
     */
    @Test
    void skipsScriptAndStyleContent() {
        String html = "<script>var 错误 = '不要改我';</script><style>.cls{颜色:red}</style><p>真正的正文内容在这里</p>";
        List<HtmlTextUtil.Segment> segs = HtmlTextUtil.segments(html);

        assertEquals(1, segs.size(), "只应剩下正文那一段");
        assertEquals("真正的正文内容在这里", segs.get(0).text());
    }

    /**
     * 过短片段（如单独的「好。」「嗯」）不送审：没有上下文，改错概率高于改对概率
     */
    @Test
    void dropsTooShortSegments() {
        String txt = "好。\n嗯。\n这一句足够长可以被送去校对。";
        List<HtmlTextUtil.Segment> segs = HtmlTextUtil.segments(txt);

        assertEquals(1, segs.size());
        assertEquals("这一句足够长可以被送去校对。", segs.get(0).text());
    }

    /**
     * 空输入与纯标签输入不得抛异常
     */
    @Test
    void blankAndTagOnlyAreSafe() {
        assertTrue(HtmlTextUtil.segments(null).isEmpty());
        assertTrue(HtmlTextUtil.segments("").isEmpty());
        assertTrue(HtmlTextUtil.segments("   \n  ").isEmpty());
        assertTrue(HtmlTextUtil.segments("<p></p><br/>").isEmpty());

        String html = "<p>正文内容在这里</p>";
        assertEquals(html, HtmlTextUtil.rebuild(html, HtmlTextUtil.segments(html), null));
        assertEquals(html, HtmlTextUtil.rebuild(html, HtmlTextUtil.segments(html), Map.of()));
    }

    /**
     * 坐标不合法（过期片段表）时必须跳过而不是覆盖到错误位置
     */
    @Test
    void rejectsStaleSegmentCoordinates() {
        String html = "<p>第一段正文内容</p><p>第二段正文内容</p>";
        List<HtmlTextUtil.Segment> stale = HtmlTextUtil.segments("<p>完全不同的另一篇正文内容啊</p>");

        Map<Integer, String> fix = new HashMap<>();
        fix.put(0, "被篡改的文字内容");
        String out = HtmlTextUtil.rebuild(html, stale, fix);

        // 越界坐标被跳过，原文保持不变
        assertEquals(html, out);
    }

    /**
     * 未解码的实体（如 &amp;#133;）留在原串里也不会破坏行结构与坐标
     */
    @Test
    void unknownEntitiesKeepOffsets() {
        String html = "<p>他拔出长剑&amp;#133;冲向敌人</p><p>后面的正文内容保持不动</p>";
        List<HtmlTextUtil.Segment> segs = HtmlTextUtil.segments(html);

        for (HtmlTextUtil.Segment s : segs) {
            assertEquals(html.substring(s.start(), s.end()), s.text()
                    .replace("&", "&amp;"), "未解码实体应保持原串形态");
        }
    }

}
