package com.pcdd.sonovel.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ConfigUtils;

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
            Console.error(render("书源规则初始化失败，请检查配置项 source-id 是否正确。{}", "red"), e.getMessage());
            System.exit(1);
        }

        // 将 JSON 转换为 Rule 对象，并应用默认值
        this.rule = applyDefaultRule(JSONUtil.toBean(jsonStr, Rule.class));
        // 使用配置文件中的配置，若为空则使用默认配置
        this.config = Opt.ofNullable(config).orElse(ConfigUtils.config());
    }

    private Rule applyDefaultRule(Rule rule) {
        Rule.Search ruleSearch = rule.getSearch();
        Rule.Book ruleBook = rule.getBook();
        Rule.Toc ruleToc = rule.getToc();
        Rule.Chapter ruleChapter = rule.getChapter();

        // baseUri
        if (ruleSearch != null && StrUtil.isEmpty(ruleSearch.getBaseUri())) {
            ruleSearch.setBaseUri(rule.getUrl());
        }
        if (ruleBook != null && StrUtil.isEmpty(ruleBook.getBaseUri())) {
            ruleBook.setBaseUri(rule.getUrl());
        }
        if (ruleToc != null && StrUtil.isEmpty(ruleToc.getBaseUri())) {
            ruleToc.setBaseUri(rule.getUrl());
        }
        if (ruleChapter != null && StrUtil.isEmpty(ruleChapter.getBaseUri())) {
            ruleChapter.setBaseUri(rule.getUrl());
        }

        // timeout
        if (ruleSearch != null && ruleSearch.getTimeout() == null) {
            ruleSearch.setTimeout(10);
        }
        if (ruleBook != null && ruleBook.getTimeout() == null) {
            ruleBook.setTimeout(10);
        }
        if (ruleToc != null && ruleToc.getTimeout() == null) {
            ruleToc.setTimeout(15);
        }
        if (ruleChapter != null && ruleChapter.getTimeout() == null) {
            ruleChapter.setTimeout(10);
        }

        return rule;
    }

}