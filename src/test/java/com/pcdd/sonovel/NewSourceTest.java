package com.pcdd.sonovel;

import com.pcdd.sonovel.convert.ChapterConverter;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
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
    public static final String BOOK_URL = "https://69shuba.cx/book/58911.htm";
    public static final String CHAPTER_URL = "https://69shuba.cx/txt/58911/38355485";

    static {
        // 覆盖原配置
        config.setSourceId(7);
        config.setExtName("txt");
    }

    @Test
    void searchParse() {
        List<SearchResult> parse = new SearchResultParser(config).parse("斗罗大陆");
        parse.forEach(System.out::println);
    }

    @Test
    void bookParse() {
        Book parse = new BookParser(config).parse(BOOK_URL);
        System.out.println(parse);
    }

    @Test
    void catalogParse() {
        List<Chapter> parse = new CatalogParser(config).parse(BOOK_URL);
        parse.forEach(System.out::println);
    }

    @Test
    void chapterParse() {
        Chapter chapter = Chapter.builder().url(CHAPTER_URL).build();
        Chapter beforeFiltration = new ChapterParser(config).parse(chapter);
        Chapter afterFiltration = new ChapterConverter(config).convert(beforeFiltration);
        System.out.println(afterFiltration.getContent());
    }

}