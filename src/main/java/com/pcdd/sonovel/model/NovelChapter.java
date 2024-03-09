package com.pcdd.sonovel.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author pcdd
 */
@Data
@Builder
public class NovelChapter {

    private String url;
    private Integer chapterNo;
    private String title;
    private String content;

}