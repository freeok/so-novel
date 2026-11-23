package com.pcdd.sonovel.utils;

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
import java.time.Duration;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        return StrUtil.isBlank(text) ? null : text.replaceAll("[\\p{C}\\p{Cf}\\p{Co}\\p{Zl}\\p{Zp}\\u200B\\uFEFF]", "");
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

    // 搜索频率间隔提示（笔趣阁系 CMS，如少年小说网）：搜索间隔【20】秒，请于 22:27:51 后再进行搜索！
    private final Pattern SEARCH_INTERVAL_HINT = Pattern.compile(
            "搜索间隔【(\\d+)】秒，请于\\s*(\\d{1,2}):(\\d{2}):(\\d{2})\\s*后再进行搜索");
    // 通用搜索间隔提示（如笔趣阁365：alert('搜索间隔15秒,请稍后重新搜索！')），只能取间隔秒数、无目标时钟
    private final Pattern SEARCH_INTERVAL_GENERIC = Pattern.compile(
            "搜索间隔[【]?(\\d+)[】]?秒");

    /**
     * 检测搜索频率间隔提示，返回建议等待秒数（含 2s 余量）；无提示返回 0。
     * <p>
     * 该类站点在两次搜索间隔不足时返回 HTTP 200，但结果区只有提示文本（无结果 li），
     * 若不处理会被当成"无结果"静默丢弃。支持两种提示：带"请于 HH:mm:ss"的精确提示（按时钟差值等待），
     * 以及仅"搜索间隔 N 秒"的通用提示（等待完整间隔）。
     */
    public long detectSearchInterval(String html) {
        if (StrUtil.isBlank(html)) return 0;

        // 优先：带"请于 HH:mm:ss"的精确提示，可按本地时钟差值等待
        Matcher m = SEARCH_INTERVAL_HINT.matcher(html);
        if (m.find()) {
            int interval = Integer.parseInt(m.group(1));
            try {
                LocalTime target = LocalTime.of(
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)));
                long byClock = Duration.between(LocalTime.now(), target).getSeconds() + 2;
                // 时钟异常（目标已过/偏差过大）时回退到完整间隔
                if (byClock >= 0 && byClock <= interval + 5L) {
                    return Math.max(0, byClock);
                }
            } catch (Exception ignore) {
            }
            return interval + 2L;
        }

        // 通用：仅间隔秒数（无目标时钟），等待完整间隔 + 2s 余量
        Matcher g = SEARCH_INTERVAL_GENERIC.matcher(html);
        if (g.find()) {
            return Integer.parseInt(g.group(1)) + 2L;
        }
        return 0;
    }

}