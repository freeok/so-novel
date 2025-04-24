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
    private boolean needProxy;
    private boolean disabled;

    private Search search;
    private Book book;
    private Toc toc;
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
        private String author;
        private String category;
        private String latestChapter;
        private String lastUpdateTime;
        private String status;
        private String wordCount;
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
        private String lastUpdateTime;
        private String status;
        private String wordCount;
    }

    @Data
    public static class Toc {
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
        // 下一页的 HTML 元素
        private String nextPage;
        // 位于 JS 中的下一页链接
        private String nextPageInJs;
    }

}