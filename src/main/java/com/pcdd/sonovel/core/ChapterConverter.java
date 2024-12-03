package com.pcdd.sonovel.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.model.Rule;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author pcdd
 * Created at 2024/3/17
 */
@AllArgsConstructor
public class ChapterConverter {

    private final ConfigBean config;
    private final TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("templates", TemplateConfig.ResourceMode.CLASSPATH));

    public Chapter convert(Chapter chapter, String extName) {
        String content = formatContent(new ChapterFilter(config.getSourceId()).filter(chapter.getContent()));

        if ("txt".equals(extName)) {
            // 全角空格，用于首行缩进
            String ident = "\u3000".repeat(2);
            Matcher matcher = Pattern.compile("<p>(.*?)</p>").matcher(content);
            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                result.append(ident).append(matcher.group(1)).append("\n");
            }

            content = chapter.getTitle() + "\n".repeat(2) + result;
        }
        if ("epub".equals(extName) || "html".equals(extName)) {
            chapter.setContent(content);
            content = templateRender(chapter, extName);
        }

        chapter.setContent(content);
        return chapter;
    }

    /**
     * 根据扩展名渲染对应模板
     */
    private String templateRender(Chapter chapter, String extName) {
        // 符合 epub 标准的模板
        Template template = engine.getTemplate(StrUtil.format("chapter_{}.flt", extName));
        Map<String, String> map = new HashMap<>();
        map.put("title", chapter.getTitle());
        map.put("content", chapter.getContent());

        return template.render(map);
    }

    private String formatContent(String content) {
        Rule.Chapter rule = new Source(config.getSourceId()).rule.getChapter();

        // 标签闭合
        if (Boolean.TRUE.equals(rule.getParagraphTagClosed())) {
            // <p>段落</p>
            if ("p".equals(rule.getParagraphTag())) {
                return content;
            } else { // 非<p>的闭合标签，替换为<p>标签
                return content.replaceAll("<(?!p\\b)([^>]+)>(.*?)</\\1>", "<p>$2</p>");
            }
        }
        // 标签不闭合，用某个标签分隔，例如：段落1<br><br>段落2
        String tag = rule.getParagraphTag();
        StringBuilder sb = new StringBuilder();

        for (String s : content.replaceAll("\\s+", "").split(tag)) {
            if (!s.isBlank()) sb.append("<p>").append(s).append("</p>");
        }

        return sb.toString();
    }

}
