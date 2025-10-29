package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * @author pcdd
 * Created at 2024/11/30
 */
@Data
public class AppConfig {

    private String version;

    // [global]
    private Integer autoUpdate;
    private String ghProxy;

    // [download]
    private String downloadPath;
    private String extName;
    private String txtEncoding;
    private Integer preserveChapterCache;

    // [source]
    private String language;
    private String activeRules;
    private Integer sourceId;
    private Integer searchLimit;
    private Integer searchFilter;

    // [crawl]
    private Integer concurrency;
    private Integer minInterval;
    private Integer maxInterval;
    private Integer enableRetry;
    private Integer maxRetries;
    private Integer retryMinInterval;
    private Integer retryMaxInterval;

    // [web]
    private Integer webEnabled;
    private Integer webPort;

    // [cookie]
    private String qidianCookie;

    // [proxy]
    private Integer proxyEnabled;
    private String proxyHost;
    private Integer proxyPort;

}