package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * @author pcdd
 * Created at 2024/3/10
 */
@Data
public class Rule {

    private int id;
    private String url;
    private String name;
    private String comment;
    private String type;
    private String language;

    private Search search;
    private Book book;
    private Catalog catalog;
    private Chapter chapter;

    @Data
    public static class Search {
        private Integer timeout;
        private String url;
        private String method;
        private String data;
        private String cookies;
        private String result;
        private String bookName;
        private String latestChapter;
        private String author;
        private String update;
        // 搜索结果是否分页
        private boolean pagination;
        private String nextPage;
        private Integer limitPage;
    }

    @Data
    public static class Book {
        private Integer timeout;
        private String url;
        private String bookName;
        private String author;
        private String intro;
        private String category;
        private String coverUrl;
        private String latestChapter;
        private String latestUpdate;
        private String isEnd;
    }

    @Data
    public static class Catalog {
        private Integer timeout;
        private String url;
        private String result;
        private Integer offset;
        private boolean isDesc;
        // 目录是否分页
        private boolean pagination;
        private String nextPage;
    }

    @Data
    public static class Chapter {
        private Integer timeout;
        // 用于测试
        private String title;
        private String content;
        private boolean paragraphTagClosed;
        private String paragraphTag;
        private String filterTxt;
        private String filterTag;
        // 章节是否分页
        private boolean pagination;
        private String nextPage;
    }

}