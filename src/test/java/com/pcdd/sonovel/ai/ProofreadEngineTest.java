package com.pcdd.sonovel.ai;

import com.pcdd.sonovel.model.AiConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 校对引擎安全防线的测试（纯单元，不联网）。
 * <p>
 * 这个功能的最大风险不是「改不对」，而是「改坏了没人发现」，
 * 因此 AI 的每条建议都必须过校验闸门。测试用一个「故意乱来的假 AI」
 * 逐条验证闸门是否会拦住，以及通过闸门的替换是否真的精确落到原文位置。
 */
class ProofreadEngineTest {

    /**
     * 可控假 AI：按脚本返回固定建议，记录收到的提示词以便断言背景是否注入
     */
    static class FakeSuggester implements ProofreadEngine.Suggester {
        final List<LlmClient.Replacement> scripted = new ArrayList<>();
        String lastBackground;
        String lastText;
        boolean throwError;
        /**
         * true = 只有第一次请求返回建议（用于多块场景下避免重复应用干扰断言）
         */
        boolean onlyFirst;
        int calls;

        FakeSuggester add(String find, String replace, String reason) {
            scripted.add(new LlmClient.Replacement(find, replace, reason));
            return this;
        }

        @Override
        public List<LlmClient.Replacement> suggest(String background, String text) throws Exception {
            this.lastBackground = background;
            this.lastText = text;
            this.calls++;
            if (throwError) throw new IllegalStateException("模拟服务不可用");
            if (onlyFirst && calls > 1) return List.of();
            return scripted;
        }
    }

    /**
     * 构造可用配置（指向一个假的 base-url，配合 FakeSuggester 不会真发请求）
     */
    static AiConfig usable() {
        AiConfig cfg = new AiConfig();
        cfg.setEnabled(1);
        cfg.setBaseUrl("http://127.0.0.1:1/v1");
        cfg.setApiKey("test-key");
        cfg.setModel("test-model");
        cfg.setChunkSize(1200);
        cfg.setMinInterval(0);
        return cfg;
    }

    static BookGlossary glossary() {
        BookGlossary g = new BookGlossary();
        g.setBookName("诡秘之主");
        g.setAuthor("爱潜水的乌贼");
        g.setSummary("蒸汽与机械的浪潮中，克莱恩·莫雷蒂穿越到异世界，成为序列者。");
        g.setSourceNote("维基百科《诡秘之主》");
        g.setResearched(true);
        g.addProtected("克莱恩");
        g.addProtected("莫雷蒂");
        g.addProtected("序列");
        g.addDomainWord("源质");
        return g;
    }

    @Test
    void appliesLegitTypoFix() {
        FakeSuggester fake = new FakeSuggester().add("他看着眼前的大海", "他看着眼前的大海雾", "漏字");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>清晨，他看着眼前的大海，久久无言。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals("<p>清晨，他看着眼前的大海雾，久久无言。</p>", out.content);
        assertEquals(1, out.edits.size());
        assertTrue(out.rejected.isEmpty(), "合法建议不应被拒绝：" + out.rejected);
    }

    /**
     * 背景资料必须进入提示词，否则专有名词保护无从谈起
     */
    @Test
    void injectsBackgroundIntoPrompt() {
        FakeSuggester fake = new FakeSuggester();
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);
        engine.proofread("第一章", "<p>克莱恩缓缓睁开双眼，打量着这间陌生的卧室。</p>", "zh-CN");

        assertNotNull(fake.lastBackground);
        assertTrue(fake.lastBackground.contains("诡秘之主"), "应含书名");
        assertTrue(fake.lastBackground.contains("克莱恩"), "应含百科摘要/专名");
        assertTrue(fake.lastBackground.contains("本章标题：第一章"), "应含章节标题");
        assertTrue(fake.lastBackground.contains("保持简体"), "应含简繁约束");
    }

    @Test
    void rejectsChangeToProtectedTerm() {
        // 把专名「克莱恩」当成错字改成「克菜恩」——必须拦住
        FakeSuggester fake = new FakeSuggester().add("克莱恩睁开眼", "克菜恩睁开眼", "错字");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>克莱恩睁开眼，看见天花板上的烛台。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content, "专名被改动时必须放弃该建议");
        assertTrue(out.edits.isEmpty());
        assertEquals(1, out.rejected.size());
        assertTrue(out.rejected.get(0).contains("专有名词"), out.rejected.toString());
    }

    @Test
    void rejectsHighFrequencyDomainWordChange() {
        FakeSuggester fake = new FakeSuggester().add("源质", "原质", "疑似错字");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>源质是这个世界最根本的力量，无人可以否认它的存在。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertTrue(out.rejected.get(0).contains("高频用词"), out.rejected.toString());
    }

    /**
     * 幻觉：find 不在原文里，必须拒绝，且绝不能凭空插入文本
     */
    @Test
    void rejectsHallucinatedFragment() {
        FakeSuggester fake = new FakeSuggester().add("根本不存在的句子", "根本不存在的序子", "错字");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>夜色降临，城里的钟声敲了十二下。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertTrue(out.rejected.get(0).contains("幻觉"), out.rejected.toString());
    }

    /**
     * 歧义：同一片段出现多次，无法确定改哪一处，必须拒绝
     */
    @Test
    void rejectsAmbiguousFragment() {
        FakeSuggester fake = new FakeSuggester().add("他笑得很勉强", "他哭得很勉强", "改情绪");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>听到这话，他笑得很勉强。</p><p>过了半晌，他笑得很勉强。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertTrue(out.rejected.get(0).contains("无法唯一定位"), out.rejected.toString());
    }

    /**
     * 同一片段内重复出现同样不可定位
     */
    @Test
    void rejectsFragmentRepeatedInsideOneSegment() {
        FakeSuggester fake = new FakeSuggester().add("他笑得很勉强", "他哭得很勉强", "改情绪");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>他笑得很勉强，过了半晌，他笑得很勉强，最终什么也没说。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertTrue(out.edits.isEmpty(), "同段重复应被拒绝而不是应用");
        assertTrue(out.rejected.get(0).contains("无法唯一定位"), out.rejected.toString());
    }

    /**
     * 越界：整段改写句式必须拦住（长度差与编辑距离双重限制）
     */
    @Test
    void rejectsWholeSentenceRewrite() {
        FakeSuggester fake = new FakeSuggester().add(
                "他缓缓站起身来走向窗边推开窗户",
                "他起身踱步至窗前将窗子一把推开",
                "润色");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>他缓缓站起身来走向窗边推开窗户，夜风灌了进来。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertFalse(out.rejected.isEmpty());
    }

    /**
     * 结构安全：建议里涉及 HTML 标签一律拒绝
     */
    @Test
    void rejectsHtmlTagTouch() {
        FakeSuggester fake = new FakeSuggester().add("笼罩着整座山谷</p>", "笼罩着整座山城</p>", "错字");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>清晨的雾气笼罩着整座山谷，很美。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertTrue(out.rejected.get(0).contains("HTML 标签"), out.rejected.toString());
    }

    /**
     * 跨段落建议拒绝（换行会让回填结构混乱）
     */
    @Test
    void rejectsCrossParagraphFragment() {
        FakeSuggester fake = new FakeSuggester().add("上一段结尾\n下一段开头", "上一段结尾，下一段开头", "合并段落");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>上一段结尾</p><p>这是一段足够长的文字用来通过片段长度过滤。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertTrue(out.rejected.get(0).contains("跨段落"), out.rejected.toString());
    }

    /**
     * 简繁转换不是错别字：向简体书引入繁体字形要拒绝（逐字字形判定，
     * 不依赖 HanLP 简繁往返 —— 实测「练/炼/練」为多对多映射，往返比较会漏判）
     */
    @Test
    void rejectsTraditionalSimplifiedConversion() {
        FakeSuggester fake = new FakeSuggester().add("后山修炼", "後山修煉", "改为繁体");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>他每日都在后山修炼，从未间断过一天。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertTrue(out.rejected.get(0).contains("繁体字形"), out.rejected.toString());
    }

    /**
     * 单字判定：明确简体/明确繁体/两岸同形字/非汉字 四类
     */
    @Test
    void scriptClassification() {
        // 两岸同形（转简转繁都不变）无法判定，返回 null 交回正常规则
        assertEquals(null, ProofreadEngine.scriptOf("修"));
        assertEquals(null, ProofreadEngine.scriptOf("人"));
        // 明确简体：转繁会变、转简不变
        assertEquals(Boolean.FALSE, ProofreadEngine.scriptOf("国"));
        assertEquals(Boolean.FALSE, ProofreadEngine.scriptOf("莱"));
        // 明确繁体：转简会变、转繁不变
        assertEquals(Boolean.TRUE, ProofreadEngine.scriptOf("國"));
        assertEquals(Boolean.TRUE, ProofreadEngine.scriptOf("練"));
        assertEquals(null, ProofreadEngine.scriptOf("A"));            // 非汉字
        assertFalse(ProofreadEngine.isTraditional("zh-CN"));
        assertTrue(ProofreadEngine.isTraditional("zh-TW"));
        assertTrue(ProofreadEngine.isTraditional("zh-Hant"));

        assertTrue(ProofreadEngine.introducesForeignScript("后山", "後山", false));
        assertFalse(ProofreadEngine.introducesForeignScript("后山", "后山", false));
        /**
         * 最关键的一条：本功能存在的理由就是修「克菜恩 → 克莱恩」这类形近错字。
         * 莱 是简体字形，绝不能被字形体系约束误拦。
         */
        assertFalse(ProofreadEngine.introducesForeignScript("克菜恩", "克莱恩", false),
                "简体书里 菜→莱 是正常错字修正，不应被判为引入繁体");
        assertFalse(ProofreadEngine.introducesForeignScript("修练", "修炼", false),
                "练→炼 属简体书内部用字，不应被拦");
    }

    /**
     * 端到端验证上面那条最要紧的形近错字真的能被修正
     */
    @Test
    void fixesLookalikeTypoInProperName() {
        FakeSuggester fake = new FakeSuggester().add("克菜恩", "克莱恩", "形近错字");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>克菜恩缓缓睁开双眼，打量着这间陌生的卧室。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals("<p>克莱恩缓缓睁开双眼，打量着这间陌生的卧室。</p>", out.content);
        assertTrue(out.rejected.isEmpty(), out.rejected.toString());
    }

    /**
     * 缩进必须保留：TXT 正文行首的全角空格不参与校对，也不能被吃掉
     */
    @Test
    void preservesIndentationAndNewlines() {
        FakeSuggester fake = new FakeSuggester().add("他看着眼前的大海", "他看着眼前的大雾", "错字");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String txt = "第一章 开场\n\n　　他看着眼前的大海，久久无言。\n　　海风腥咸。\n";
        ProofreadEngine.Outcome out = engine.proofread("第一章 开场", txt, "zh-CN");

        assertTrue(out.content.contains("　　他看着眼前的大雾，久久无言。"), out.content);
        assertTrue(out.content.startsWith("第一章 开场\n\n　　"), "标题与缩进应原样保留");
        assertEquals(txt.chars().filter(c -> c == '\n').count(),
                out.content.chars().filter(c -> c == '\n').count(), "换行数量不得变化");
    }

    /**
     * 跨块歧义消除：同名片段分处两块时，模型只看过当前块，因此应当在本块内唯一命中并应用，
     * 而不是因为「全书有两次」而整条拒绝（成品 TXT 全书一个文件，否则常见短句几乎全会被误拒）。
     */
    @Test
    void appliesWithinChunkWhenSameFragmentExistsInAnotherChunk() {
        FakeSuggester fake = new FakeSuggester().add("少年握紧了拳头", "少年松开了拳头", "前后矛盾");
        // 只在第一次请求返回建议，避免多块重复应用干扰断言
        fake.onlyFirst = true;
        AiConfig cfg = usable();
        cfg.setChunkSize(200);   // 逼出多块
        ProofreadEngine engine = new ProofreadEngine(cfg, glossary(), fake);

        // 两句之间塞足够长的内容（>chunkSize 下限 200），确保三者被切到不同的块
        String filler = "这一整段都是没有错误的正常正文，用来把上下两句挤到不同的送审块里。".repeat(8);
        String content = "<p>少年握紧了拳头。</p><p>" + filler + "</p><p>少年握紧了拳头。</p>";

        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(1, out.edits.size(), "应在模型看到的那一块内唯一命中：" + out.edits);
        assertTrue(out.rejected.isEmpty(), out.rejected.toString());
        // 只应改第一块那处，第二块原文保持不动
        assertTrue(out.content.startsWith("<p>少年松开了拳头。</p>"), out.content);
        assertTrue(out.content.endsWith("<p>少年握紧了拳头。</p>"), "后续块不应被本块的建议波及");
    }

    /**
     * 进度回调必须覆盖全部块，前端进度条才不会停在半路
     */
    @Test
    void reportsChunkProgress() {
        AiConfig cfg = usable();
        cfg.setChunkSize(200);
        List<int[]> seen = new ArrayList<>();
        ProofreadEngine engine = new ProofreadEngine(cfg, glossary(), new FakeSuggester())
                .withListener(new ProofreadEngine.Listener() {
                    @Override
                    public void onChunks(int completed, int total) {
                        seen.add(new int[]{completed, total});
                    }
                });

        // 三句各自成块：每段 filler 270 字 > chunkSize(200)，必然独占一块
        String filler = "这一整段都是没有错误的正常正文，用来把内容挤到不同的送审块里。".repeat(10);
        String content = "<p>" + filler + "</p><p>" + filler + "</p><p>" + filler + "</p>";
        engine.proofread("第一章", content, "zh-CN");

        assertFalse(seen.isEmpty(), "必须上报进度");
        int total = seen.get(0)[1];
        assertTrue(total > 1, "内容应被切成多块，实际 " + total);
        assertEquals(total, seen.get(seen.size() - 1)[0], "最后一次进度应到达总块数");
        for (int i = 0; i < seen.size(); i++) {
            assertEquals(i + 1, seen.get(i)[0], "进度需连续递增");
            assertEquals(total, seen.get(i)[1], "总数需稳定");
        }
    }

    /**
     * AI 故障降级：请求抛异常时保留原文并记录，绝不半途写坏文件
     */
    @Test
    void aiFailureKeepsOriginal() {
        FakeSuggester fake = new FakeSuggester();
        fake.throwError = true;
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>克莱恩缓缓睁开双眼，打量着这间陌生的卧室。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content, "AI 不可用必须原样返回");
    }

    /**
     * 未配置 AI（无 key）时只做乱码修复，不应发起任何请求
     */
    @Test
    void garbledOnlyWhenAiNotConfigured() {
        AiConfig cfg = usable();
        cfg.setApiKey("");
        FakeSuggester fake = new FakeSuggester().add("任意", "建议", "不应被调用");
        ProofreadEngine engine = new ProofreadEngine(cfg, glossary(), fake);

        String content = "<p>他拔出长剑锟斤拷冲向敌人</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertFalse(engine.aiEnabled());
        assertNull(fake.lastText, "未配置 key 时不得调用 AI");
        assertEquals("<p>他拔出长剑冲向敌人</p>", out.content);
        assertTrue(out.garbledFixed > 0);
    }

    /**
     * 完全没有 AI 建议时，输出必须与输入完全一致
     */
    @Test
    void noSuggestionsMeansNoChange() {
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), new FakeSuggester());
        String content = "<p>这是一段没有任何错误的正常正文，讲的是一个少年清晨练剑。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(content, out.content);
        assertTrue(out.changed() == false);
    }

    @Test
    void editDistanceHelperWorks() {
        assertEquals(0, ProofreadEngine.editDistance("abc", "abc"));
        assertEquals(1, ProofreadEngine.editDistance("克莱恩", "克菜恩"));
        assertEquals(2, ProofreadEngine.editDistance("abcd", "ab"));
    }

    /**
     * 多次改动落在一章内：应各自精确命中，互不干扰
     */
    @Test
    void appliesMultipleFixesInOneChapter() {
        FakeSuggester fake = new FakeSuggester()
                .add("少年握紧了拳头", "少年握紧了拳有", "错字")
                .add("远方传来号角声", "远方传来叫号声", "颠倒");
        ProofreadEngine engine = new ProofreadEngine(usable(), glossary(), fake);

        String content = "<p>少年握紧了拳头，指节发白。</p><p>远方传来号角声，旷野上硝烟弥漫。</p>";
        ProofreadEngine.Outcome out = engine.proofread("第一章", content, "zh-CN");

        assertEquals(2, out.edits.size(), out.edits.toString());
        assertEquals("<p>少年握紧了拳有，指节发白。</p><p>远方传来叫号声，旷野上硝烟弥漫。</p>", out.content);
        assertTrue(out.rejected.isEmpty(), out.rejected.toString());
    }

}
