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

    // [download]
    private String downloadPath;
    private String extName;
    private Integer preserveChapterCache;

    // [source]
    private String language;
    private String activeRules;
    private Integer sourceId;
    private Integer searchLimit;

    // [crawl]
    private Integer threads;
    private Integer minInterval;
    private Integer maxInterval;
    private Integer enableRetry;
    private Integer maxRetries;
    private Integer retryMinInterval;
    private Integer retryMaxInterval;

    // [proxy]
    private Integer proxyEnabled;
    private String proxyHost;
    private Integer proxyPort;

    // [web]
    private Integer webEnabled;
    private Integer webPort;

}