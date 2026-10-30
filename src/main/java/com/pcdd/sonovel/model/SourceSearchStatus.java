package com.pcdd.sonovel.model;

import lombok.Builder;
import lombok.Data;

/**
 * 聚合搜索时，单个书源的连接/搜索状态（用于前端逐源展示是否超时/失败/限流）
 */
@Data
@Builder
public class SourceSearchStatus {

    private int id;
    private String name;
    private String url;

    /**
     * ok：成功；empty：连通但无结果；timeout：连接超时；
     * error：其他错误；interval：触发搜索间隔/频率限制
     */
    private String status;
    private int count;
    private long elapsedMs;
    private String message;

}
