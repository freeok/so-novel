package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
@Data
public class Book {

    private String url;
    private String bookName;
    private String author;
    private String intro;
    private String category;
    private String coverUrl;
    private String latestChapter;
    private String lastUpdateTime;
    private String status;
    private String wordCount;

}