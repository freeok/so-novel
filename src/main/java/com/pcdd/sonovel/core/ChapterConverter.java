package com.pcdd.sonovel.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import cn.hutool.http.HtmlUtil;
import com.pcdd.sonovel.model.Chapter;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

import static com.pcdd.sonovel.util.ConfigConst.SOURCE_ID;

/**
 * @author pcdd
 */
@UtilityClass
public class ChapterConverter {

    private final TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("templates", TemplateConfig.ResourceMode.CLASSPATH));

    public Chapter convert(Chapter chapter, String extName) {
        String content = new ChapterFilter(SOURCE_ID).filter(chapter.getContent());
        chapter.setContent(content);

        if ("txt".equals(extName)) {
            content = chapter.getTitle() + "\n\n" + HtmlUtil.cleanHtmlTag(content)
                    .replace("&nbsp;", " ");
        }
        if ("epub".equals(extName) || "html".equals(extName)) {
            content = templateRender(chapter, extName);
        }

        chapter.setContent(content);
        return chapter;
    }

    /**
     * 根据扩展名渲染对应模板
     */
    private static String templateRender(Chapter chapter, String extName) {
        String content = chapter.getContent();
        // 符合 epub 标准的模板
        Template template = engine.getTemplate(StrUtil.format("chapter_{}.flt", extName));
        Map<String, String> map = new HashMap<>();
        map.put("title", chapter.getTitle());
        // 构建符合 epub 标准的正文格式，仅适用于书源 1
        content = "<br>".concat(content.replaceAll("&nbsp;|\\s+", ""))
                .replaceAll("<br>(.*?)<br>", "<p>$1</p>")
                .replaceAll("<p></p>|<br>", "");
        map.put("content", content);
        content = template.render(map);
        return content;
    }

}
