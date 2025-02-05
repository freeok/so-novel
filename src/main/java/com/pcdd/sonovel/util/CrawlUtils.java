package com.pcdd.sonovel.util;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.script.ScriptUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.ContentType;
import lombok.experimental.UtilityClass;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pcdd.sonovel.model.ContentType.*;


/**
 * @author pcdd
 * Created at 2024/11/28
 */
@UtilityClass
public class CrawlUtils {

    /**
     * 使用查询条件选择元素
     */
    public Elements select(Element e, String query) {
        // 分割查询条件以提取 XPath 或 CSS 查询
        String actualQuery = StrUtil.subBefore(query, "##", false);
        // 根据查询条件选择元素
        if (actualQuery.startsWith("/")) {
            return e.selectXpath(actualQuery);
        }
        return e.select(actualQuery);
    }

    /**
     * 执行 JS 脚本并返回处理结果
     */
    public String invokeJs(String query, String input) {
        if (StrUtil.isEmpty(query)) {
            return input;
        }
        // 分割 JS 脚本查询条件，提取函数名
        String[] split = query.split("##");
        // 无 JS，返回原始输入
        if (split.length == 1) {
            return input;
        }
        return (String) ScriptUtil.invoke(split[1], "func", input);
    }

    /**
     * 根据查询条件选择元素并执行可能的 JS 脚本
     */
    public String selectAndInvokeJs(Element e, String query, ContentType contentType) {
        if (StrUtil.isEmpty(query)) {
            return getContent(e, contentType); // 直接获取内容
        }

        // 分割查询条件
        String[] split = query.split("##");
        String actualQuery = split[0]; // 实际的选择器或 XPath

        // 根据查询条件选择元素
        Elements elements = select(e, actualQuery);

        // 如果没有找到任何元素，返回空字符串
        if (elements.isEmpty()) {
            return "";
        }

        // 获取选中元素的内容
        String result = getContents(elements, contentType);

        // 如果查询条件包含 JS 执行部分，调用它
        if (split.length == 2) {
            return invokeJs(query, result);
        }

        return result;
    }

    public String getStrAndInvokeJs(Element e, String query, ContentType contentType) {
        // 先获取元素的内容
        String result = CrawlUtils.getContent(e, contentType);
        // 如果查询条件包含 JS 执行部分，调用它
        if (StrUtil.isNotEmpty(query)) {
            result = CrawlUtils.invokeJs(query, result);
        }
        return result;
    }

    /**
     * 获取元素的内容
     */
    public String getContent(Element el, ContentType contentType) {
        return switch (contentType) {
            case TEXT -> el.text();
            case HTML -> el.html();
            case ATTR_SRC -> el.attr(ATTR_SRC.getValue());
            case ATTR_HREF -> el.attr(ATTR_HREF.getValue());
            case ATTR_CONTENT -> el.attr(ATTR_CONTENT.getValue());
        };
    }

    /**
     * 获取元素的多个内容
     */
    private String getContents(Elements els, ContentType contentType) {
        return switch (contentType) {
            case TEXT -> els.text();
            case HTML -> els.html();
            case ATTR_SRC -> els.attr(ATTR_SRC.getValue());
            case ATTR_HREF -> els.attr(ATTR_HREF.getValue());
            case ATTR_CONTENT -> els.attr(ATTR_CONTENT.getValue());
        };
    }

    // 有的 href 是相对路径，需要拼接为完整路径
    public String normalizeUrl(String s, String host) {
        return URLUtil.normalize(Validator.isUrl(s) ? s : host + s, true, true);
    }

    // POST 构建 Body，GET 构建 Query Parameters
    public Map<String, String> buildData(String jsonStr, String... args) {
        Map<String, String> params = new HashMap<>();
        AtomicInteger i = new AtomicInteger(0);

        JSONUtil.parseObj(jsonStr)
                .forEach((key, value) -> {
                    if ("%s".equals(value)) {
                        if (i.get() < args.length) {
                            params.put(key, args[i.getAndIncrement()]);
                        }
                    } else {
                        params.put(key, value.toString());
                    }
                });

        return params;
    }

    public Map<String, String> buildCookies(String cookies) {
        Map<String, String> map = new HashMap<>();

        JSONUtil.parseObj(cookies)
                .forEach((key, value) -> map.put(key, value.toString()));

        return map;
    }

    public Connection.Method buildMethod(String method) {
        return switch (method.toLowerCase()) {
            case "get" -> Connection.Method.GET;
            case "post" -> Connection.Method.POST;
            case "put" -> Connection.Method.PUT;
            case "delete" -> Connection.Method.DELETE;
            case "patch" -> Connection.Method.PATCH;
            case "head" -> Connection.Method.HEAD;
            case "options" -> Connection.Method.OPTIONS;
            case "trace" -> Connection.Method.TRACE;
            default -> throw new IllegalArgumentException("Unsupported request method: " + method);
        };
    }

    public long randomInterval(AppConfig config, boolean isRetry) {
        return ThreadLocalRandom.current().nextLong(
                isRetry ? config.getRetryMinInterval() : config.getMinInterval(),
                isRetry ? config.getRetryMaxInterval() : config.getMaxInterval());
    }

}