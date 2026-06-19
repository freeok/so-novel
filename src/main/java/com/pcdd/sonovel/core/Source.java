package com.pcdd.sonovel.core;

import cn.hutool.core.lang.Opt;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.SourceUtils;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class Source {

    public final Rule rule;
    public final AppConfig config;

    // 配置文件读取书源 id
    public Source(AppConfig config) {
        this(SourceUtils.getRule(config.getSourceId()), config);
    }

    public Source(int sourceId) {
        this(SourceUtils.getRule(sourceId), null);
    }

    public Source(Rule rule, AppConfig config) {
        this.rule = rule;
        // 使用配置文件中的配置，若为空则使用默认配置
        this.config = Opt.ofNullable(config).orElse(AppConfigLoader.APP_CONFIG);
        applyCrawlSettings(rule.getCrawl());
    }

    private void applyCrawlSettings(Rule.Crawl crawl) {
        if (crawl == null) return;
        Opt.ofNullable(crawl.getConcurrency()).ifPresent(this.config::setConcurrency);
        Opt.ofNullable(crawl.getMinInterval()).ifPresent(this.config::setMinInterval);
        Opt.ofNullable(crawl.getMaxInterval()).ifPresent(this.config::setMaxInterval);
        Opt.ofNullable(crawl.getMaxAttempts()).ifPresent(this.config::setMaxRetries);
        Opt.ofNullable(crawl.getRetryMinInterval()).ifPresent(this.config::setRetryMinInterval);
        Opt.ofNullable(crawl.getRetryMaxInterval()).ifPresent(this.config::setRetryMaxInterval);
    }

}