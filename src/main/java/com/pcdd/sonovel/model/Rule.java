package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * @author pcdd
 */
@Data
public class Rule {

    private int id;
    private String url;
    private Book book;
    private Chapter chapter;
    private Search search;
    private ParamName paramName;

    @Data
    public static class Book {
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

    @Data
    public static class Chapter {
        private String url;
        private Integer chapterNo;
        private String title;
        private String content;
    }

    @Data
    public static class Search {
        private String url;
        private String method;
        // 请求载荷的 key
        private ParamName paramName;
        // 以下字段不同书源可能不同
        private String result;
        private String bookName;
        private String latestChapter;
        private String author;
        private String update;
    }

    @Data
    public static class ParamName {
        // 关键字的参数名
        private String keyword;
    }

}
