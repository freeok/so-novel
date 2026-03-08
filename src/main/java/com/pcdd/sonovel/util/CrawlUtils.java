package com.pcdd.sonovel.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.AppConfig;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import okhttp3.*;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author pcdd
 * Created at 2024/11/28
 */
@UtilityClass
public class CrawlUtils {

    // Cloudflare 常见拦截标题
    private final Set<String> CF_STRONG_TITLES = Set.of(
            "Just a moment...",
            "403 Forbidden",
            "Attention Required",
            "Checking your browser before accessing"
    );

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

    /**
     * 清理不可见字符：控制字符、格式控制符、私有区 PUA 字符 (导致中文乱码的根源)
     */
    public String cleanInvisibleChars(String text) {
        return StrUtil.isEmpty(text) ? null : text.replaceAll("[\\p{C}\\p{Cf}\\p{Co}\\p{Zl}\\p{Zp}\\u200B\\uFEFF]", "");
    }

    @SneakyThrows
    public Response request(OkHttpClient client, String url, int timeout) {
        Call call = client.newCall(new Request.Builder()
                .url(url)
                .addHeader(Header.USER_AGENT.toString(), RandomUA.generate())
                .addHeader(Header.REFERER.toString(), URLUtil.getHost(URLUtil.url(url)).toString())
                .build()
        );
        call.timeout().timeout(timeout, TimeUnit.SECONDS);

        return call.execute();
    }

    @SneakyThrows
    public Response request(OkHttpClient client, Request.Builder builder, int timeout) {
        URL url = builder.getUrl$okhttp().url();
        String referer = URLUtil.getHost(url).toString();
        Call call = client.newCall(builder
                .addHeader(Header.USER_AGENT.toString(), RandomUA.generate())
                .addHeader(Header.REFERER.toString(), referer)
                .build()
        );
        call.timeout().timeout(timeout, TimeUnit.SECONDS);

        return call.execute();
    }

    /**
     * 网页是否有 Cloudflare 真人验证
     */
    public boolean hasCf(Document document) {
        if (document == null) return false;
        String title = document.title();
        return CF_STRONG_TITLES.contains(title);
    }

}