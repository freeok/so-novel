package com.pcdd.sonovel.ai;

import cn.hutool.core.util.StrUtil;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.pcdd.sonovel.utils.LogUtils;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 专有名词表聚合器：把「外部百科」与「本书正文统计」两路信息合成一张保护名单。
 * <p>
 * 两路互补，缺一不可：
 * <ul>
 *   <li><b>百科</b>权威但覆盖差 —— 只有知名作品有条目，且不会列出全部配角与功法名。</li>
 *   <li><b>正文统计</b>覆盖全但噪声多 —— 用 HanLP 分词的专名词性（人名/机构/其他专名）
 *       结合词频抽取，能捕捉到百科里没有的「本书自创词」，如功法、境界、组织名。</li>
 * </ul>
 * 对网文而言第二路尤其重要：绝大多数书根本没有百科条目，此时保护名单只能来自正文本身。
 *
 * @author pcdd
 */
@UtilityClass
public class GlossaryCollector {

    /**
     * 正文专名候选：词频下限与总量上限（提示词预算有限，宁少勿滥）
     */
    private final int MIN_FREQ = 3;
    private final int MAX_DOMAIN_WORDS = 80;
    private final int MAX_PROTECTED_FROM_TEXT = 40;

    /**
     * 可信专名词性：人名、机构名、其他专名（自创词多数落在这里）、热新词
     */
    private final Set<String> NAME_NATURES = Set.of("nr", "nr1", "nr2", "nrj", "nrt", "nt", "nz", "x", "t");

    /**
     * 基于百科调研结果与正文样本构建词表。
     *
     * @param bookName   书名
     * @param author     作者
     * @param intro      书源详情页简介（无百科时的唯一背景来源）
     * @param sampleText 正文样本文本（建议取前若干章拼接，控制在几万字内）
     */
    public BookGlossary collect(String bookName, String author, String intro, String sampleText, int aiMaxTerms) {
        BookGlossary g = new BookGlossary();
        g.setBookName(bookName);
        g.setAuthor(author);

        // 1) 外部百科
        WikiResearcher.Research r = WikiResearcher.research(bookName, author);
        if (r.isHit()) {
            g.setSummary(r.getSummary());
            g.setSourceNote(r.getSourceNote());
            g.setResearched(true);
            r.getNames().forEach(g::addProtected);
        } else if (StrUtil.isNotBlank(intro)) {
            // 2) 降级：书源自带简介也胜过什么都没有
            g.setSummary(HtmlTextUtil.text(intro));
            g.setSourceNote("书源简介");
        }

        // 书名与作者本身就是最高优先级的专名
        g.addProtected(bookName);
        if (StrUtil.isNotBlank(author)) {
            g.addProtected(author);
        }
        if (StrUtil.isNotBlank(intro)) {
            extractNames(HtmlTextUtil.text(intro), 1, Integer.MAX_VALUE)
                    .forEach(e -> g.addProtected(e.getKey()));
        }

        // 3) 正文统计专名
        if (StrUtil.isNotBlank(sampleText)) {
            List<Map.Entry<String, Integer>> terms = extractNames(sampleText, MIN_FREQ, MAX_DOMAIN_WORDS);
            for (Map.Entry<String, Integer> e : terms) {
                g.addDomainWord(e.getKey());
                // 高频且百科未覆盖的专名，升级为强保护
                if (e.getValue() >= MAX_FREQ_PROTECT && g.getProtectedTerms().size() < aiMaxTerms) {
                    g.addProtected(e.getKey());
                }
            }
        }

        LogUtils.info("AI 校对词表：专有名词 {} 个，高频词 {} 个（背景来源：{}）",
                g.getProtectedTerms().size(), g.getDomainWords().size(),
                g.isResearched() ? g.getSourceNote() : "正文统计/书源简介");
        return g;
    }

    private final int MAX_FREQ_PROTECT = 10;

    /**
     * 分词抽取专名：词性命中 + 词频达标，按频次降序
     */
    private List<Map.Entry<String, Integer>> extractNames(String text, int minFreq, int limit) {
        Map<String, Integer> freq = new HashMap<>();
        try {
            List<Term> terms = HanLP.newSegment()
                    .enableNameRecognize(true)
                    .enableOrganizationRecognize(true)
                    .seg(text);
            for (Term t : terms) {
                String word = t.word;
                if (word == null || word.length() < 2 || word.length() > 8) continue;
                if (t.nature == null || !NAME_NATURES.contains(t.nature.toString())) continue;
                if (!isMostlyChinese(word)) continue;
                freq.merge(word, 1, Integer::sum);
            }
        } catch (Throwable e) {
            // HanLP 词典缺失等环境问题不应阻断下载
            LogUtils.info("正文专名统计不可用：{}", e.getMessage());
            List<Term> fallback = StandardTokenizer.segment(text);
            for (Term t : fallback) {
                String word = t.word;
                if (word == null || word.length() < 2 || word.length() > 8 || !isMostlyChinese(word)) continue;
                freq.merge(word, 1, Integer::sum);
            }
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(freq.entrySet());
        list.removeIf(e -> e.getValue() < minFreq);
        list.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed()
                .thenComparing(Map.Entry::getKey));
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    private boolean isMostlyChinese(String s) {
        int cjk = 0;
        for (int cp : s.codePoints().toArray()) {
            if (cp >= 0x4E00 && cp <= 0x9FFF) cjk++;
        }
        return cjk * 2 >= s.length();
    }

}
