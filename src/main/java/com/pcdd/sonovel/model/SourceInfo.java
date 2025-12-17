package com.pcdd.sonovel.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceInfo {

    private Integer id;
    private String name;
    private String url;
    private String comment;
    private boolean needProxy;
    private boolean disabled;
    private Integer delay;
    private Integer code;

}