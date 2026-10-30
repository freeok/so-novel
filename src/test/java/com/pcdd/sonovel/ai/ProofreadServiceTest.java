package com.pcdd.sonovel.ai;

import com.pcdd.sonovel.context.BookContext;
import com.pcdd.sonovel.model.AiConfig;
import com.pcdd.sonovel.model.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 校对调度器的离线端到端用例（不需要 AI 服务、不联网）。
 * <p>
 * 重点锁两件事：
 * ① 校对路径必须自带 BookContext —— LogUtils 依赖它取书名生成日志文件，
 * 而只有下载流程的 Crawler 会设置。此前 Web/CLI/TUI 校对一调用就 NPE，整本书校对直接失败。
 * ② 未配置 AI 服务时只能做确定性乱码修复，且不得浪费网络与全文分词去做背景调研。
 */
class ProofreadServiceTest {

    @TempDir
    Path tmp;

    /**
     * 只开乱码修复（无 key，usable=false）
     */
    private AppConfig garbledOnlyConfig() {
        AiConfig ai = new AiConfig();
        ai.setEnabled(1);
        ai.setFixGarbled(1);
        ai.setBaseUrl("");
        ai.setApiKey("");
        AppConfig cfg = new AppConfig();
        cfg.setAi(ai);
        cfg.setLanguage("zh-CN");
        return cfg;
    }

    @Test
    void bindsContextSoLoggingWorks() {
        ProofreadService svc = new ProofreadService(garbledOnlyConfig());
        assertNull(BookContext.get(), "用例开始前不应有残留上下文");

        svc.bindContext("测试书名", "测试作者");

        assertNotNull(BookContext.get(), "校对路径必须自己绑定上下文，否则 LogUtils 会 NPE");
        assertEquals("测试书名", BookContext.get().getBookName());
        try {
            // 真正的断言：这句在下载流程之外也必须能跑通（曾经必炸）
            assertDoesNotThrow(() -> com.pcdd.sonovel.utils.LogUtils.info("单元测试日志 {}", 1));
        } finally {
            BookContext.clear();
        }
    }

    /**
     * 下载流水线已绑定上下文时不得被清理掉，否则后续成品合并会失去上下文
     */
    @Test
    void doesNotClearInheritedContext() throws Exception {
        File f = writeBook("001_第一章.txt", "他拔出长剑锟斤拷冲向敌人，克莱恩缓缓睁开双眼看向远方。");

        ProofreadService svc = new ProofreadService(garbledOnlyConfig());
        var book = new com.pcdd.sonovel.model.Rule.Book();
        book.setBookName("已在下载中的书");
        book.setAuthor("佚名");
        BookContext.set(book);
        try {
            svc.proofreadFiles(List.of(f), "已在下载中的书", "佚名", "",
                    tmp.toFile(), false, ProofreadService.Progress.NOOP);
            assertNotNull(BookContext.get(), "外部绑定的上下文不应被校对流程清除");
            assertEquals("已在下载中的书", BookContext.get().getBookName());
        } finally {
            BookContext.clear();
        }
    }

    /**
     * 未配置 AI 时的完整成品校对链路：修好乱码、留备份、出审计，且不留残上下文
     */
    @Test
    void proofreadsWholeBookOffline() throws Exception {
        File f = writeBook("冒烟测试书(测试作者).txt",
                "书名：冒烟测试书\n\n作者：测试作者\n\n　　他拔出长剑锟斤拷\uff85冲向敌人，"
                        + "克莱恩缓缓睁开双眼打量着这间陌生的卧室，久久无言。\n\n　　第二段正常正文，"
                        + "内容与\uff85无关但也包含乱码：测试\uff88\uff89完成。\n");
        String original = Files.readString(f.toPath(), StandardCharsets.UTF_8);

        ProofreadService svc = new ProofreadService(garbledOnlyConfig());
        String summary = svc.proofreadFiles(List.of(f), "冒烟测试书", "测试作者", "简介占位",
                f.getParentFile(), true, ProofreadService.Progress.NOOP);

        assertNotNull(summary);
        assertTrue(summary.contains("乱码清理"), "摘要应体现乱码修复数量：" + summary);

        String fixed = Files.readString(f.toPath(), StandardCharsets.UTF_8);
        assertFalse(fixed.contains("锟斤拷"), "不可逆乱码应被清除");
        // 正文其余部分必须逐字保留
        assertTrue(fixed.contains("克莱恩缓缓睁开双眼打量着这间陌生的卧室"), fixed);
        assertTrue(fixed.startsWith("书名：冒烟测试书"), "文件头不得被破坏");

        // 备份在位且内容等于原文
        File bak = new File(f.getAbsolutePath() + ".proofread.bak");
        assertTrue(bak.exists(), "成品模式必须写回前备份");
        assertEquals(original, Files.readString(bak.toPath(), StandardCharsets.UTF_8));

        // 审计日志生成
        assertTrue(new File(f.getParent(), "冒烟测试书 (测试作者) 校对记录.log").exists(),
                "应产出可复核的校对记录");

        assertNull(BookContext.get(), "自行绑定的上下文必须在结束后清理，避免污染调用线程");
    }

    /**
     * 没开 AI 也不该去动文件：文件保持原样
     */
    @Test
    void skippedEntirelyWhenDisabled() throws Exception {
        File f = writeBook("安静的一本书(佚名).txt", "　　这一段完全是正常正文，没有任何乱码问题存在。\n");
        String before = Files.readString(f.toPath(), StandardCharsets.UTF_8);

        AppConfig cfg = garbledOnlyConfig();
        cfg.getAi().setEnabled(0);
        ProofreadService svc = new ProofreadService(cfg);
        assertFalse(svc.enabled(), "enabled=0 时校对不得启动");

        svc.proofreadFiles(List.of(f), "安静的一本书", "佚名", "", f.getParentFile(), true,
                ProofreadService.Progress.NOOP);
        assertEquals(before, Files.readString(f.toPath(), StandardCharsets.UTF_8));
        assertFalse(new File(f.getAbsolutePath() + ".proofread.bak").exists(),
                "未改动就不该留下备份文件");
    }

    private File writeBook(String name, String content) throws Exception {
        Path dir = tmp.resolve("downloads");
        Files.createDirectories(dir);
        Path p = dir.resolve(name);
        Files.writeString(p, content, StandardCharsets.UTF_8);
        return p.toFile();
    }

}
