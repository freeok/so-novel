package com.pcdd.sonovel.core;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.dsl.DslBootstrap;
import com.pcdd.sonovel.dsl.core.StepEngine;
import com.pcdd.sonovel.model.ContentType;
import lombok.experimental.UtilityClass;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author pcdd
 * Created at 2026/7/1
 */
@UtilityClass
public class HtmlExtractor {

    /**
     * DSL 执行引擎
     */
    private final StepEngine DSL_ENGINE = DslBootstrap.createEngine();

    /**
     * 执行 DSL 并返回处理结果，等价于 func(input)
     *
     * @param dsl 格式：xxx@js:code;@java:code
     */
    public String executeDsl(String dsl, String input) {
        return DSL_ENGINE.run(dsl, input);
    }

    /**
     * 根据 CSS Selector 或 XPath 查询元素
     * <p>
     * 等价于 document.select(query) | document.selectXpath(query)
     */
    public Elements select(Element el, String query) {
        // 分割查询条件以提取 XPath 或 CSS 查询
        String selector = StrUtil.subBefore(query, "@", false);

        boolean isXPath = selector.matches("^(/|//|\\().*");
        return isXPath ? el.selectXpath(selector) : el.select(selector);
    }

    public String extract(Element el, String query) {
        return extract(el, query, ContentType.TEXT);
    }

    /**
     * 查询元素并提取内容，同时支持 DSL 后置处理
     * <p>
     * 执行流程：
     * 1. 解析 selector（CSS/XPath）
     * 2. 提取内容（text/html/attr）
     * 3. 交由 DSL 引擎进行二次处理
     */
    public String extract(Element el, String query, ContentType contentType) {
        if (el == null || StrUtil.isBlank(query) || contentType == null) {
            return null;
        }

        String cleanQuery = stripQuery(query);

        Elements elements = select(el, cleanQuery);
        if (elements.isEmpty()) return "";


        String raw = elements.size() == 1
                ? getContent(elements.first(), contentType)
                : getContent(elements, contentType);

        // DSL 二次处理（如 JS / Java / 自定义脚本）
        return executeDsl(query, raw);
    }

    /**
     * 获取元素内容 + 可选 DSL 处理，等价于 func(element.(text|html|attr)())
     * <p>
     * 执行流程：
     * 1. 已有 Element
     * 2. 提取内容（text/html/attr）
     * 3. 交由 DSL 引擎进行二次处理
     */
    public String extractContent(Element el, String query, ContentType contentType) {
        // 先获取元素的内容
        String value = getContent(el, contentType);
        // 如果 query 包含 @js、@java，调用它
        return StrUtil.isNotEmpty(query) ? executeDsl(query, value) : value;
    }

    /**
     * 提取元素内容（统一入口）
     */
    private String getContent(Object obj, ContentType type) {
        if (obj instanceof Element el) {
            return switch (type) {
                case TEXT -> el.text();
                case HTML -> el.html();
                case ATTR_SRC, ATTR_HREF -> el.absUrl(type.getValue());
                // 切勿改为 absUrl
                case ATTR_CONTENT, ATTR_VALUE -> el.attr(type.getValue());
            };
        }

        if (obj instanceof Elements els) {
            return switch (type) {
                case TEXT -> els.text();
                case HTML -> els.html();
                case ATTR_SRC, ATTR_HREF, ATTR_CONTENT, ATTR_VALUE -> els.attr(type.getValue());
            };
        }

        return "";
    }

    /**
     * 去除 selector 中的属性语法（@href / @src），仅保留 CSS/XPath 选择器
     */
    private static String stripQuery(String query) {
        if (query == null) return null;

        int atIndex = query.indexOf('@');
        return atIndex > 0 ? query.substring(0, atIndex) : query;
    }

}