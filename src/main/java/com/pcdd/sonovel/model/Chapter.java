package com.pcdd.sonovel.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author pcdd
 * Created at 2024/3/10
 */
@Data
@Builder
public class Chapter {

    private String url;
    private String title;
    private String content;
    private Integer order;

}