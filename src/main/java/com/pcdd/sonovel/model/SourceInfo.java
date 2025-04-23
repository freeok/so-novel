package com.pcdd.sonovel.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceInfo {
    private Integer id;
    private String url;
    private String name;
    private Integer delay;
    private Integer code;
}