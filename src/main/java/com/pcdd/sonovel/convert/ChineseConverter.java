package com.pcdd.sonovel.convert;

import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

/**
 * @author pcdd
 * Created at 2024/12/31
 */
@UtilityClass
public class ChineseConverter {

    // 通用转换方法，接受转换函数作为参数
    private <T> T convert(T obj, Function<String, String> convertFunc) {
        if (obj == null) {
            return null;
        }

        // 如果是 Book 类型
        if (obj instanceof Book book) {
            book.setBookName(convertIfNotNull(book.getBookName(), convertFunc));
            book.setAuthor(convertIfNotNull(book.getAuthor(), convertFunc));
            book.setIntro(convertIfNotNull(book.getIntro(), convertFunc));
            book.setCategory(convertIfNotNull(book.getCategory(), convertFunc));
            book.setLatestChapter(convertIfNotNull(book.getLatestChapter(), convertFunc));
            book.setLatestUpdate(convertIfNotNull(book.getLatestUpdate(), convertFunc));
            book.setIsEnd(convertIfNotNull(book.getIsEnd(), convertFunc));
            return (T) book;
        }

        // 如果是 Chapter 类型
        if (obj instanceof Chapter chapter) {
            chapter.setTitle(convertIfNotNull(chapter.getTitle(), convertFunc));
            chapter.setContent(convertIfNotNull(chapter.getContent(), convertFunc));
            return (T) chapter;
        }

        // 如果是 SearchResult 类型
        if (obj instanceof SearchResult sr) {
            sr.setBookName(convertIfNotNull(sr.getBookName(), convertFunc));
            sr.setAuthor(convertIfNotNull(sr.getAuthor(), convertFunc));
            sr.setIntro(convertIfNotNull(sr.getIntro(), convertFunc));
            sr.setLatestChapter(convertIfNotNull(sr.getLatestChapter(), convertFunc));
            sr.setLatestUpdate(convertIfNotNull(sr.getLatestUpdate(), convertFunc));
            return (T) sr;
        }

        return obj;  // 如果是其他类型，直接返回
    }

    // 用于判断是否为 null 并进行转换
    private String convertIfNotNull(String value, Function<String, String> convertFunc) {
        return value != null ? convertFunc.apply(value) : null;
    }

    /* ================================================== Book ================================================== */

    public Book t2s(Book book) {
        return convert(book, HanLP::t2s);
    }

    public Book t2tw(Book book) {
        return convert(book, HanLP::t2tw);
    }

    public Book s2t(Book book) {
        return convert(book, HanLP::s2t);
    }

    public Book s2tw(Book book) {
        return convert(book, HanLP::s2tw);
    }

    public Book s2hk(Book book) {
        return convert(book, HanLP::s2hk);
    }

    /* ================================================== Chapter ================================================== */

    public Chapter t2s(Chapter chapter) {
        return convert(chapter, HanLP::t2s);
    }

    public Chapter t2tw(Chapter chapter) {
        return convert(chapter, HanLP::t2tw);
    }

    public Chapter s2t(Chapter chapter) {
        return convert(chapter, HanLP::s2t);
    }

    public Chapter s2tw(Chapter chapter) {
        return convert(chapter, HanLP::s2tw);
    }

    public Chapter s2hk(Chapter chapter) {
        return convert(chapter, HanLP::s2hk);
    }

    /* ================================================== SearchResult ================================================== */

    public SearchResult t2s(SearchResult sr) {
        return convert(sr, HanLP::t2s);
    }

    public SearchResult t2tw(SearchResult sr) {
        return convert(sr, HanLP::t2tw);
    }

    public SearchResult s2t(SearchResult sr) {
        return convert(sr, HanLP::s2t);
    }

    public SearchResult s2tw(SearchResult sr) {
        return convert(sr, HanLP::s2tw);
    }

    public SearchResult s2hk(SearchResult sr) {
        return convert(sr, HanLP::s2hk);
    }

}