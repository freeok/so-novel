package com.pcdd.sonovel.model;

import lombok.Data;

@Data
public class ClientReport {

    private Long id;
    private String username;
    private String hostName;
    private String macAddress;
    private String osName;
    private String osArch;
    private String osVersion;
    private String localIp;
    // 从 CF-Connecting-IP 获取
    // private String publicIp;
    private String appVersion;
    private String createdAt;
    private String updatedAt;

}