package com.pcdd.sonovel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author pcdd
 * Created at 2025/2/5
 */
@Getter
@AllArgsConstructor
public enum ContentType {

    TEXT("text"),
    HTML("html"),
    ATTR_SRC("src"),
    ATTR_HREF("href"),
    ATTR_CONTENT("content"),
    ATTR_VALUE("value"),
    ;

    private final String value;

}