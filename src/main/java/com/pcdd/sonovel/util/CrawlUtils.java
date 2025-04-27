package com.pcdd.sonovel.util;

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