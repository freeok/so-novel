package com.pcdd.sonovel.web.model;

import lombok.Builder;

/**
 * @author pcdd
 * Created at 2025/8/7
 */
@Builder
public class DownloadProgressInfo {

    private String type;
    private Integer total;
    private Long index;

}