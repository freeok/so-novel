package com.pcdd.sonovel.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author pcdd
 * Created at 2022-05-23 23:28:56
 */
@Data
@Builder
public class SearchResultLine {

    private String bookName;
    private String author;
    private String latestChapter;
    private String latestUpdate;
    private String link;

}
