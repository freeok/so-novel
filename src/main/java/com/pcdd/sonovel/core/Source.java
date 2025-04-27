package com.pcdd.sonovel.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Opt;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.OkHttpUtils;
import com.pcdd.sonovel.util.RandomUA;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.jline.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class Source {

    public final Rule rule;
    public final AppConfig config;
    public final OkHttpClient client;

    // 配置文件读取书源 id
    public Source(AppConfig config) {
        this(config.getSourceId(), config);
    }

    public Source(int id) {
        this(id, null);
    }

    // 自定义书源 id，用于测试
    public Source(int id, AppConfig config) {
        String jsonStr = null;

        try {
            // 根据 sourceId 获取对应书源规则
            jsonStr = ResourceUtil.readUtf8Str("rule/rule-" + id + ".json");
        } catch (Exception e) {
            Console.error(render("书源规则初始化失败，请检查配置项 source-id 是否正确。{}", "red"), e.getMessage());
            System.exit(1);
        }

        // 将 JSON 转换为 Rule 对象，并应用默认值
        this.rule = applyDefaultTimeouts(JSONUtil.toBean(jsonStr, Rule.class));
        // 使用配置文件中的配置，若为空则使用默认配置
        this.config = Opt.ofNullable(config).orElse(ConfigUtils.config());
        this.client = OkHttpUtils.createClient(config, this.rule.isIgnoreSsl());
    }

    private Rule applyDefaultTimeouts(Rule rule) {
        return rule;
    }

    @SneakyThrows
    public Response request(String url) {
        Request req = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", RandomUA.generate())
                .build();
        return client.newCall(req).execute();
    }

    @SneakyThrows
    public Response request(Request.Builder builder) {
        Request req = builder
                .addHeader("User-Agent", RandomUA.generate())
                .build();
        return client.newCall(req).execute();
    }

}