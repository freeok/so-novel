package com.pcdd.sonovel.utils;

import cn.hutool.core.util.StrUtil;

import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * DSL = Domain Specific Language（领域特定语言）
 * <p>
 * 用字符串表达网页解析 + 脚本处理流程
 */
@UtilityClass
public class HtmlUtils {

    /**
     * 清除所有元素及其子元素的属性，防止标签与属性间的空格干扰解析。
     */
    public String clearAllAttributes(String html) {
        Element body = Jsoup.parse(html).body();

        for (Element el : body.select("*")) {
            el.clearAttributes();
        }
        // 删除 Element#html 产生的 \n
        return StrUtil.cleanBlank(body.html());
    }

    /**
     * 删除指定 CSS 选择器匹配的 HTML 标签
     *
     * @param html     原始 HTML
     * @param cssQuery 例如 .tt-title, #id, [style], script, div，详见 https://jsoup.org/cookbook/extracting-data/selector-syntax
     * @return 清理后的 HTML
     */
    public String removeTags(String html, String cssQuery) {
        if (StrUtil.isBlank(html) || StrUtil.isBlank(cssQuery)) {
            return html;
        }

        Document doc = Jsoup.parseBodyFragment(html);
        doc.select(cssQuery).remove();

        return doc.body().html();
    }

}