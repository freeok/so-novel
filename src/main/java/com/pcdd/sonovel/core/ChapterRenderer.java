package com.pcdd.sonovel.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Chapter;
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
public class ChapterRenderer {

    private final AppConfig config;
    private static final Pattern P_TAG_PATTERN = Pattern.compile("<p>(.*?)</p>", Pattern.DOTALL);
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateUtil.createEngine(new TemplateConfig("templates", TemplateConfig.ResourceMode.CLASSPATH));

    public Chapter process(Chapter chapter) {
        Chapter filtered = new ChapterFilter(config).filter(chapter);
        String content = new ChapterFormatter(config).format(filtered.getContent());

        chapter.setTitle(filtered.getTitle());
        chapter.setContent(switch (config.getExtName()) {
            case "txt" -> renderTxtFormat(filtered.getTitle(), content);
            case "epub", "html", "pdf" -> renderTemplateFormat(filtered.getTitle(), content, config.getExtName());
            default -> content;
        });
        return chapter;
    }

    private String renderTxtFormat(String title, String htmlContent) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = P_TAG_PATTERN.matcher(htmlContent);
        // 全角空格，用于首行缩进2字符
        String indent = "\u3000".repeat(2);

        while (matcher.find()) {
            sb.append(indent).append(matcher.group(1)).append('\n');
        }
        return title + "\n\n" + sb;
    }

    /**
     * 根据扩展名渲染对应模板
     */
    private String renderTemplateFormat(String title, String content, String ext) {
        Template template = TEMPLATE_ENGINE.getTemplate(StrUtil.format("chapter_{}.flt", ext));
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        return template.render(map);
    }

}