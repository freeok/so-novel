package com.pcdd.sonovel.util;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;
import org.jsoup.Connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author pcdd
 * Created at 2024/11/28
 */
@UtilityClass
public class CrawlUtils {

    // 有的 href 是相对路径，需要拼接为完整路径
    public String normalizeUrl(String s, String host) {
        return URLUtil.normalize(Validator.isUrl(s) ? s : host + s, true, true);
    }

    public Map<String, String> buildParams(String body, String keyword) {
        Map<String, String> params = new HashMap<>();

        JSONUtil.parseObj(body)
                .forEach((key, value) -> {
                    if (key.equals("kw")) params.put(value.toString(), keyword);
                    else params.put(key, value.toString());
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

    public void randomSleep(long min, long max) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(min, max));
    }

}
