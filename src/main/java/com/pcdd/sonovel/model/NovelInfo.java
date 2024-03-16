package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * @author pcdd
 */
@Data
public class NovelInfo {

    private String url;
    private String bookName;
    private String author;
    private String description;
    private String category;
    private String coverUrl;
    private String latestChapter;
    private String latestUpdate;
    private String isEnd;
    private String catalog;

}
