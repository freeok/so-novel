package com.pcdd.sonovel.core;

import cn.hutool.core.lang.Opt;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ConfigWatcher;
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
        this.config = Opt.ofNullable(config).orElse(ConfigWatcher.getConfig());

        // 规则爬取配置覆盖默认配置
        Rule.Crawl crawl = rule.getCrawl();
        if (crawl != null) {
            if (crawl.getThreads() != null) {
                this.config.setThreads(crawl.getThreads());
            }
            if (crawl.getMinInterval() != null) {
                this.config.setMinInterval(crawl.getMinInterval());
            }
            if (crawl.getMaxInterval() != null) {
                this.config.setMaxInterval(crawl.getMaxInterval());
            }
            if (crawl.getMaxAttempts() != null) {
                this.config.setMaxRetries(crawl.getMaxAttempts());
            }
            if (crawl.getRetryMinInterval() != null) {
                this.config.setRetryMinInterval(crawl.getRetryMinInterval());
            }
            if (crawl.getRetryMaxInterval() != null) {
                this.config.setRetryMaxInterval(crawl.getRetryMaxInterval());
            }
        }
    }

}