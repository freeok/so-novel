package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * @author pcdd
 * Created at 2024/11/30
 */
@Data
public class AppConfig {

    private String version;

    // [base]
    private String language;
    private String downloadPath;
    private String extName;
    private Integer autoUpdate;
    private Integer interactiveMode;
    private Integer sourceId;
    private Integer searchLimit;

    // [crawl]
    private Integer threads;
    private Integer minInterval;
    private Integer maxInterval;

    // [retry]
    private Integer maxRetryAttempts;
    private Integer retryMinInterval;
    private Integer retryMaxInterval;

    // [proxy]
    private Integer proxyEnabled;
    private String proxyHost;
    private Integer proxyPort;

}