package com.pcdd.sonovel.ai;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 一本书的「背景知识包」：由百科调研 + 本地文本统计共同产出，
 * 既作为 AI 校对的提示词上下文，也作为专有名词白名单（防止 AI 把人名功法名「纠」成别字）。
 *
 * @author pcdd
 */
@Data
public class BookGlossary {

    private String bookName;
    private String author;

    /**
     * 百科摘要（简介 + 世界观/角色梗概），用于喂给 AI
     */
    private String summary;

    /**
     * 背景知识来源说明，例如 "维基百科《诡秘之主》"，便于审计与展示
     */
    private String sourceNote;

    /**
     * 是否成功取到外部百科资料
     */
    private boolean researched;

    /**
     * 专有名词白名单：这些词 AI 不得改动
     */
    private final Set<String> protectedTerms = new LinkedHashSet<>();

    /**
     * 高频词表（仅用于提示 AI「这些是本书常用词，别当错字」），容量受限
     */
    private final Set<String> domainWords = new LinkedHashSet<>();

    public void addProtected(String term) {
        if (term == null) return;
        String t = term.trim();
        if (t.length() >= 2 && t.length() <= 16) {
            protectedTerms.add(t);
        }
    }

    public void addDomainWord(String word) {
        if (word == null) return;
        String w = word.trim();
        if (w.length() >= 2 && w.length() <= 8) {
            domainWords.add(w);
        }
    }

    /**
     * 生成给 AI 的背景资料块（受长度约束，避免挤占正文 token 预算）
     */
    public String toPromptContext(int maxChars) {
        StringBuilder sb = new StringBuilder();
        sb.append("书名：").append(bookName).append('\n');
        if (author != null && !author.isBlank()) {
            sb.append("作者：").append(author).append('\n');
        }
        if (summary != null && !summary.isBlank()) {
            sb.append("背景资料（来自").append(sourceNote == null ? "百科" : sourceNote).append("）：\n")
                    .append(trimTo(summary, maxChars)).append('\n');
        } else {
            sb.append("背景资料：未检索到可靠百科条目，请以正文上下文为准，务必保守修改。\n");
        }
        if (!protectedTerms.isEmpty()) {
            sb.append("以下专有名词必须原样保留（人名/地名/功法/组织/术语）：\n")
                    .append(joinLimited(protectedTerms, 60, "、")).append('\n');
        }
        if (!domainWords.isEmpty()) {
            sb.append("以下词语在本书中高频出现，属于正常用词，不得当作错别字：\n")
                    .append(joinLimited(domainWords, 80, "、")).append('\n');
        }
        return sb.toString();
    }

    private String trimTo(String s, int max) {
        if (max <= 0 || s.length() <= max) return s;
        return s.substring(0, max) + "……";
    }

    private String joinLimited(Set<String> set, int limit, String sep) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String s : set) {
            if (i++ >= limit) {
                sb.append(sep).append("……");
                break;
            }
            if (!sb.isEmpty()) sb.append(sep);
            sb.append(s);
        }
        return sb.toString();
    }

}
