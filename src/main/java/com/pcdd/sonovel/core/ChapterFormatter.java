package com.pcdd.sonovel.core;

import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import lombok.AllArgsConstructor;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
@AllArgsConstructor
public class ChapterFormatter {

    private final AppConfig config;

    /**
     * 格式化正文排版
     */
    public String format(String content) {
        Rule.Chapter r = new Source(config).rule.getChapter();

        // <tag>段落</tag>
        if (r.isParagraphTagClosed()) {
            // 非 <p> 闭合标签（例如 <span>段落</span>）替换为 <p>
            return content.replaceAll("<(?!p\\b)([^>]+)>(.*?)</\\1>", "<p>$2</p>");
        }

        // 标签不闭合，用某个标签分隔，例如：段落1<br><br>段落2
        StringBuilder contentBuilder = new StringBuilder();
        for (String line : content.split(r.getParagraphTag())) {
            if (!line.isBlank()) {
                contentBuilder.append("<p>").append(line).append("</p>");
            }
        }

        return contentBuilder.toString();
    }

}