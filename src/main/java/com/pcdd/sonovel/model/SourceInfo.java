package com.pcdd.sonovel.model;

import lombok.Data;

@Data
public class SourceInfo {
    private Integer id;
    private String url;
    private String name;
    private Integer delay;
    private Integer code;
}