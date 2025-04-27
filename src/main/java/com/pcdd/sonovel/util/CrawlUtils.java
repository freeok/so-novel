package com.pcdd.sonovel.util;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.AppConfig;
import lombok.experimental.UtilityClass;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author pcdd
 * Created at 2024/11/28
 */
@UtilityClass
public class CrawlUtils {

    // 有的 href 是相对路径，需要拼接为完整路径
    public String normalizeUrl(String s, String host) {
        if (s == null) return null;

        if (s.matches("^http(s)?://.*")) return s;

        return URLUtil.normalize(Validator.isUrl(s) ? s : host + s, true, true);
    }

    // 构建 POST Body
    public static RequestBody buildData(String jsonStr, String... args) {
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

}