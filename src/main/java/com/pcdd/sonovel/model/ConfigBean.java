package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * @author pcdd
 * Created at 2024/11/30
 */
@Data
public class ConfigBean {

    private String version;

    // base
    private int sourceId;
    private String downloadPath;
    private String extName;
    private Boolean autoUpdate;

    // crawl
    private int threads;
    private int minInterval;
    private int maxInterval;

    // retry
    private int maxRetryAttempts;
    private int retryMinInterval;
    private int retryMaxInterval;

}