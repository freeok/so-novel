package com.pcdd.sonovel.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author pcdd
 * Created at 2022/5/23
 */
@Data
@Builder
public class SearchResult {

    private Integer sourceId;
    private String url;
    private String bookName;
    private String author;
    private String intro;
    private String category;
    private String latestChapter;
    private String lastUpdateTime;
    private String status;
    private String wordCount;

}