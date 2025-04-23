package com.pcdd.sonovel.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Opt;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.RandomUA;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import static org.jline.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class Source {

    public final Rule rule;
    public final AppConfig config;

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
            Console.error(render("书源规则初始化失败，请检查配置项 source-id 是否正确", "red"));
            Console.error(render("错误信息：{}", "red"), e.getMessage());
            System.exit(1);
        }

        // 将 JSON 转换为 Rule 对象，并应用默认值
        this.rule = applyDefaultTimeouts(JSONUtil.toBean(jsonStr, Rule.class));
        // 使用配置文件中的配置，若为空则使用默认配置
        this.config = Opt.ofNullable(config).orElse(ConfigUtils.config());
    }

    private Rule applyDefaultTimeouts(Rule rule) {
        Rule.Search ruleSearch = rule.getSearch();
        Rule.Book ruleBook = rule.getBook();
        Rule.Toc ruleToc = rule.getToc();
        Rule.Chapter ruleChapter = rule.getChapter();

        if (ruleSearch != null && ruleSearch.getTimeout() == null) {
            ruleSearch.setTimeout(15_000);
        }
        if (ruleBook != null && ruleBook.getTimeout() == null) {
            ruleBook.setTimeout(10_000);
        }
        if (ruleToc != null && ruleToc.getTimeout() == null) {
            ruleToc.setTimeout(30_000);
        }
        if (ruleChapter != null && ruleChapter.getTimeout() == null) {
            ruleChapter.setTimeout(15_000);
        }

        return rule;
    }

    public Connection jsoup(String url) {
        // 除了搜索请求，其他请求基本均为 GET
        String method = rule.getSearch() != null ? rule.getSearch().getMethod() : "GET";

        Connection conn = Jsoup.connect(url)
                .method(CrawlUtils.buildMethod(method))
                .header("User-Agent", RandomUA.generate());

        // 启用配置文件的代理地址
        if (config.getProxyEnabled() == 1) {
            conn.proxy(config.getProxyHost(), config.getProxyPort());
        }

        return conn;
    }

}