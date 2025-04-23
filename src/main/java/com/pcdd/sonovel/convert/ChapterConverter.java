package com.pcdd.sonovel.convert;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.pcdd.sonovel.core.ChapterFilter;
import com.pcdd.sonovel.core.ChapterFormatter;
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
public class ChapterConverter {

    private final AppConfig config;
    private final TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("templates", TemplateConfig.ResourceMode.CLASSPATH));

    public Chapter convert(Chapter chapter) {
        String extName = config.getExtName();
        String filteredContent = new ChapterFilter(config).filter(chapter);
        String content = new ChapterFormatter(config).format(filteredContent);

        if ("txt".equals(extName)) {
            // 全角空格，用于首行缩进
            String ident = "\u3000".repeat(2);
            Matcher matcher = Pattern.compile("<p>(.*?)</p>").matcher(content);
            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                result.append(ident)
                        .append(matcher.group(1))
                        .append("\n");
            }

            content = chapter.getTitle() + "\n".repeat(2) + result;
        }
        if (extName.matches("(?i)^(epub|html|pdf)$")) {
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
        // epub 或 html 模板
        Template template = engine.getTemplate(StrUtil.format("chapter_{}.flt", extName));
        Map<String, String> map = new HashMap<>();
        map.put("title", chapter.getTitle());
        map.put("content", chapter.getContent());

        return template.render(map);
    }

}