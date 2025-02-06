package com.pcdd.sonovel.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.script.ScriptUtil;
import com.pcdd.sonovel.model.ContentType;
import lombok.experimental.UtilityClass;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static com.pcdd.sonovel.model.ContentType.*;

@UtilityClass
public class JsoupUtils {

    private static final String JS_SEPARATOR = "##"; // JS 分隔符

    /**
     * 使用查询条件选择元素
     * <p>
     * 等价于 document.select(query) | document.selectXpath(query)
     */
    public Elements select(Element e, String query) {
        // 分割查询条件以提取 XPath 或 CSS 查询
        String actualQuery = StrUtil.subBefore(query, JS_SEPARATOR, false);
        // 根据查询条件选择元素
        return actualQuery.startsWith("/") ? e.selectXpath(actualQuery) : e.select(actualQuery);
    }

    /**
     * 执行 JS 脚本并返回处理结果
     * <p>
     * 等价于 func(input)
     */
    public String invokeJs(String query, String input) {
        if (StrUtil.isEmpty(query)) return input;
        String[] split = query.split(JS_SEPARATOR);
        return split.length == 1 ? input : (String) ScriptUtil.invoke(split[1], "func", input);
    }

    /**
     * 根据查询条件选择元素并执行可能的 JS 脚本
     * <p>
     * 等价于 func(document.select(query).(text|html|attr)())
     */
    public String selectAndInvokeJs(Element e, String query, ContentType contentType) {
        if (StrUtil.isEmpty(query)) {
            return null;
        }

        String[] split = query.split(JS_SEPARATOR);
        String actualQuery = split[0];

        // 根据查询条件选择元素
        Elements elements = select(e, actualQuery);
        if (elements.isEmpty()) return "";

        // 获取选中元素的内容
        String result = getContents(elements, contentType);

        // 如果查询条件包含 JS，调用它
        return split.length == 2 ? invokeJs(query, result) : result;
    }

    /**
     * 获取元素的内容并执行可能的 JS
     * <p>
     * 等价于 func(element.(text|html|attr)())
     */
    public String getStrAndInvokeJs(Element e, String js, ContentType contentType) {
        // 先获取元素的内容
        String result = getContent(e, contentType);
        // 如果查询条件包含 JS，调用它
        return StrUtil.isNotEmpty(js) ? invokeJs(js, result) : result;
    }

    /**
     * 获取单个元素的内容
     */
    public String getContent(Element el, ContentType contentType) {
        return getContentByType(el, contentType);
    }

    /**
     * 获取多个元素的内容
     */
    private String getContents(Elements els, ContentType contentType) {
        return getContentByType(els, contentType);
    }

    /**
     * 提取内容的公共方法
     */
    private String getContentByType(Object obj, ContentType contentType) {
        if (obj instanceof Element el) {
            return switch (contentType) {
                case TEXT -> el.text();
                case HTML -> el.html();
                case ATTR_SRC -> el.attr(ATTR_SRC.getValue());
                case ATTR_HREF -> el.attr(ATTR_HREF.getValue());
                case ATTR_CONTENT -> el.attr(ATTR_CONTENT.getValue());
            };
        } else if (obj instanceof Elements els) {
            return switch (contentType) {
                case TEXT -> els.text();
                case HTML -> els.html();
                case ATTR_SRC -> els.attr(ATTR_SRC.getValue());
                case ATTR_HREF -> els.attr(ATTR_HREF.getValue());
                case ATTR_CONTENT -> els.attr(ATTR_CONTENT.getValue());
            };
        }
        return "";
    }

}