package com.pcdd.sonovel.web.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author pcdd
 * Created at 2025/8/7
 */
@Data
@Builder
public class DownloadProgressInfo {

    private String type;
    private Integer total;
    private Long index;
    /**
     * 可选的人类可读消息（校对任务用它汇报开始/完成/失败）
     */
    private String message;

}