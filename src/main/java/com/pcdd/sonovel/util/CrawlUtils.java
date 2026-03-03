package com.pcdd.sonovel.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.AppConfig;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import okhttp3.*;
import org.jsoup.nodes.Document;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author pcdd
 * Created at 2024/11/28
 */
@UtilityClass
public class CrawlUtils {

    // 构建 POST Body
    public RequestBody buildData(String jsonStr, String... args) {
        FormBody.Builder from = new FormBody.Builder();
        AtomicInteger i = new AtomicInteger(0);

        JSONUtil.parseObj(jsonStr)
                .forEach((key, value) -> {
                    if ("%s".equals(value)) {
                        if (i.get() < args.length) {
                            from.add(key, args[i.getAndIncrement()]);
                        }
                    } else {
                        from.add(key, value.toString());
                    }
                });

        return from.build();
    }

    public long randomInterval(AppConfig config) {
        return randomInterval(config, false);
    }

    public long randomInterval(AppConfig config, boolean isRetry) {
        return ThreadLocalRandom.current().nextLong(
                isRetry ? config.getRetryMinInterval() : config.getMinInterval(),
                isRetry ? config.getRetryMaxInterval() : config.getMaxInterval());
    }

    public String cleanInvisibleChars(String text) {
        // 过滤：控制字符、格式控制符、私有区 PUA 字符 (导致中文乱码的根源)
        return StrUtil.isEmpty(text) ? null : text.replaceAll("[\\p{C}\\p{Cf}\\p{Co}\\p{Zl}\\p{Zp}\\u200B\\uFEFF]", "");
    }

    @SneakyThrows
    public Response request(OkHttpClient client, String url, int timeout) {
        Call call = client.newCall(new Request.Builder()
                .url(url)
                .addHeader("User-Agent", RandomUA.generate())
                .build()
        );
        call.timeout().timeout(timeout, TimeUnit.SECONDS);

        return call.execute();
    }

    @SneakyThrows
    public Response request(OkHttpClient client, Request.Builder builder, int timeout) {
        Call call = client.newCall(builder
                .addHeader("User-Agent", RandomUA.generate())
                .build()
        );
        call.timeout().timeout(timeout, TimeUnit.SECONDS);

        return call.execute();
    }

    /**
     * 网页是否有 Cloudflare 真人验证
     */
    public boolean hasCf(Document doc) {
        if (doc == null) return false;

        // 1. title 特征
        String title = doc.title();
        if (title.contains("Just a moment") || title.contains("Attention Required") || title.contains("Checking your browser")) {
            return true;
        }

        String html = doc.html();
        // 2. JS challenge
        if (html.contains("/cdn-cgi/challenge-platform")) return true;
        if (html.contains("__cf_chl")) return true;

        // 3. 验证 DOM
        if (doc.selectFirst("form#challenge-form") != null) return true;
        if (doc.selectFirst(".cf-browser-verification") != null) return true;
        if (doc.selectFirst("div[id*=cf-chl]") != null) return true;

        // 4. Turnstile
        if (html.contains("turnstile")) return true;

        // 5. 常见 Cloudflare 标识
        if (html.contains("Ray ID")) return true;
        if (html.contains("cloudflare") && (html.contains("error code") || html.contains("blocked"))) {
            return true;
        }

        return false;
    }

}