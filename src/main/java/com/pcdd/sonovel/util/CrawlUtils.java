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

    public Elements select(Element e, String query) {
        String[] split = query.split("##");
        if (split.length == 2) {
            query = split[0];
        }
        if (query.startsWith("/")) {
            return e.selectXpath(query);
        }
        return e.select(query);
    }

    /**
     * 调用某个 query 的 js
     */
    public String invokeJs(String query, String input) {
        if (StrUtil.isEmpty(query)) {
            return input;
        }
        String[] split = query.split("##");
        if (split.length != 2) {
            return input;
        }
        return (String) ScriptUtil.invoke(split[1], "func", input);
    }

    public String selectAndInvokeJs(Element e, String query, ContentType contentType) {
        if (StrUtil.isEmpty(query)) {
            return getStr(e, contentType);
        }

        // 分割查询条件
        String[] split = query.split("##");
        String actualQuery = split[0]; // 实际的选择器或 XPath

        // 根据查询条件选择元素
        Elements elements;
        if (actualQuery.startsWith("/")) {
            elements = e.selectXpath(actualQuery);
        } else {
            elements = e.select(actualQuery);
        }

        // 如果没有找到任何元素，返回空字符串
        if (elements.isEmpty()) {
            return "";
        }

        // 根据 contentType 获取相应内容
        String result = switch (contentType) {
            case TEXT -> elements.text();
            case HTML -> elements.html();
            case ATTR_SRC -> elements.attr(ATTR_SRC.getValue());
            case ATTR_HREF -> elements.attr(ATTR_HREF.getValue());
            case ATTR_CONTENT -> elements.attr(ATTR_CONTENT.getValue());
        };

        // 如果查询条件包含 JS 执行部分，调用它
        if (split.length == 2) {
            return (String) ScriptUtil.invoke(split[1], "func", result);
        }

        return result;
    }


    public String getStr(Element el, ContentType contentType) {
        return switch (contentType) {
            case TEXT -> el.text();
            case HTML -> el.html();
            case ATTR_SRC -> el.attr(ATTR_SRC.getValue());
            case ATTR_HREF -> el.attr(ATTR_HREF.getValue());
            case ATTR_CONTENT -> el.attr(ATTR_CONTENT.getValue());
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