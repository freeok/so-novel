package com.pcdd.sonovel;

import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.parse.CatalogParser;
import com.pcdd.sonovel.parse.ChapterParser;
import com.pcdd.sonovel.parse.SearchResultParser;
import com.pcdd.sonovel.util.ConfigUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author pcdd
 * Created at 2024/12/28
 */
class NewSourceTest {

    public static final AppConfig config = ConfigUtils.config();

    static {
        config.setSourceId(5);
    }

    @Test
    void searchParse() {
        SearchResultParser sr = new SearchResultParser(config);
        List<SearchResult> parse = sr.parse("夜无疆");
        parse.forEach(System.out::println);
    }

    @Test
    void bookParse() {
        BookParser bookParser = new BookParser(config);
        Book parse = bookParser.parse("https://69shux.co/book/15119.html");
        System.out.println(parse);
    }

    @Test
    void catalogParse() {
        CatalogParser catalogParser = new CatalogParser(config);
        List<Chapter> parse = catalogParser.parse("https://69shux.co/book/15119.html");
        parse.forEach(System.out::println);
    }

    @Test
    void chapterParse() {
        Chapter chapter = Chapter.builder()
                .url("https://69shux.co/txt/15119/7685425.html")
                .title("测试章节名")
                .build();
        ChapterParser chapterParser = new ChapterParser(config);
        System.out.println(chapterParser.parse(chapter));
    }

}