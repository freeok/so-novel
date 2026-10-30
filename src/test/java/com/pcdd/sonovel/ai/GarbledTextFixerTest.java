package com.pcdd.sonovel.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 确定性乱码修复 {@link GarbledTextFixer} 的行为测试（纯单元，无网络）。
 * <p>
 * 关注两件事：能不能救回真乱码、会不会误伤正常文本（后者更重要）。
 */
class GarbledTextFixerTest {

    /**
     * UTF-8 字节被 Latin-1 误读的经典外观：「诡秘之主」-> 「è¯¡ç§˜ä¹‹ä¸»」
     */
    @Test
    void recoversLatin1MisreadUtf8() {
        String real = "诡秘之主";
        String mojibake = new String(real.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        assertNotEquals(real, mojibake, "构造的 mojibake 应与原文不同");
        assertTrue(GarbledTextFixer.looksGarbled(mojibake), "应识别为乱码");

        GarbledTextFixer.Result r = GarbledTextFixer.fix(mojibake, "zh-CN");
        assertEquals(real, r.getText(), "应还原为原文");
        assertTrue(r.getMojibakeFixed() > 0);
    }

    /**
     * 不可逆指纹（锟斤拷）只能删除，不能编造内容
     */
    @Test
    void removesIrreversibleMarkers() {
        String text = "他拔出长剑锟斤拷冲向敌人";
        GarbledTextFixer.Result r = GarbledTextFixer.fix(text, "zh-CN");
        assertEquals("他拔出长剑冲向敌人", r.getText());
        assertTrue(r.getIrreversibleRemoved() >= 1);
    }

    /**
     * U+FFFD 替换符同样删除
     */
    @Test
    void removesReplacementChar() {
        String text = "夜色\ufffd\ufffd降临";
        GarbledTextFixer.Result r = GarbledTextFixer.fix(text, "zh-CN");
        assertEquals("夜色降临", r.getText());
    }

    /**
     * 私有区字符（PUA 字体反爬）删除
     */
    @Test
    void removesPuaChars() {
        String text = "修炼\uE000功法\uF8FF有成";
        GarbledTextFixer.Result r = GarbledTextFixer.fix(text, "zh-CN");
        assertEquals("修炼功法有成", r.getText());
    }

    /**
     * 正常文本必须完全不动（幂等 + 无误伤）。
     * 这条是安全性底线：宁可漏改，不可改坏。
     */
    @Test
    void leavesCleanChineseTextUntouched() {
        String text = """
                周元站在山崖边，望着远处翻涌的云海，心中暗自思忖：
                “修炼一途，如逆水行舟，不进则退。”
                他深吸一口气，体内的元力缓缓运转，四肢百骸都传来一阵酥麻之感。
                """;
        assertFalse(GarbledTextFixer.looksGarbled(text), "正常文本不应被判为乱码");
        GarbledTextFixer.Result r = GarbledTextFixer.fix(text, "zh-CN");
        assertEquals(text, r.getText(), "正常文本必须原样返回");
        assertFalse(r.changed());
    }

    /**
     * 含英文、数字、标点的正常混排文本同样不许动
     */
    @Test
    void leavesAlphanumericUntouched() {
        String text = "他打开了 Windows 11 系统，输入密码 abc123!@# 后登录成功，耗时 3.5 秒。";
        GarbledTextFixer.Result r = GarbledTextFixer.fix(text, "zh-CN");
        assertEquals(text, r.getText());
    }

    /**
     * HTML 标签结构必须保持（只动文本，不动标签）
     */
    @Test
    void preservesHtmlTags() {
        String html = "<p>他拔出长剑锟斤拷冲向敌人</p>";
        GarbledTextFixer.Result r = GarbledTextFixer.fix(html, "zh-CN");
        assertEquals("<p>他拔出长剑冲向敌人</p>", r.getText());
    }

    /**
     * 修复一次后再修复应无变化（幂等）
     */
    @Test
    void idempotent() {
        String dirty = "他拔出长剑锟斤拷冲向敌人";
        String once = GarbledTextFixer.fix(dirty, "zh-CN").getText();
        String twice = GarbledTextFixer.fix(once, "zh-CN").getText();
        assertEquals(once, twice, "二次修复不应再改动");
    }

    @Test
    void nullAndBlankSafe() {
        assertNull(GarbledTextFixer.fix(null, "zh-CN").getText());
        assertEquals("   ", GarbledTextFixer.fix("   ", "zh-CN").getText());
        assertFalse(GarbledTextFixer.looksGarbled(null));
        assertFalse(GarbledTextFixer.looksGarbled(""));
    }

}
