package com.pcdd.sonovel.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author pcdd
 * Created at 2025/8/6
 */
@Data
public class LocalBookItem implements Serializable {

    private String name;
    private Long size;
    private Long timestamp;

}