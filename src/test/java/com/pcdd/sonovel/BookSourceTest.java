package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

/**
 * @author pcdd
 * Created at 2024/12/28
 */
// 保证测试类实例在多个测试方法间共享
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookSourceTest {

    private static final AppConfig config = ConfigUtils.config();
    public static final String DIVIDER = "=".repeat(50);
    private String bookUrl;
    private String chapterUrl;

    static {
        ConsoleLog.setLevel(Level.OFF);
        // 覆盖默认配置
        config.setLanguage("zh_CN");
        config.setExtName("txt");
        config.setThreads(-1);
    }

    @DisplayName("测试直连书源")
    @ParameterizedTest
    @CsvSource({
            "1, http://www.xbiqugu.net/130/130509/, http://www.xbiqugu.net/130/130509/48221266.html",
            "2, https://www.shuhaige.net/266625/, https://www.shuhaige.net/266625/102443757.html",
            "3, http://www.mcmssc.la/145_145199/, http://www.mcmssc.la/145_145199/57831284.html",
            "4, http://www.99xs.info/tag/129_129843/, http://www.99xs.info/tag/129_129843/47783670.html",
    })
    void testDirectSources(int sourceId, String bookUrl, String chapterUrl) {
        this.bookUrl = bookUrl;
        this.chapterUrl = chapterUrl;

        config.setSourceId(sourceId);

        searchParse("唐家三少");
        bookParse();
        chapterParse();
        catalogParse();
    }

    @DisplayName("测试代理书源")
    @ParameterizedTest
    @CsvSource({
            "5, https://69shux.co/book/54619.html, https://69shux.co/txt/54619/23367169.html",
            "6, https://quanben5.com/n/xinghedadi/, https://quanben5.com/n/xinghedadi/29882.html",
            "7, https://69shuba.cx/book/58911.htm, https://69shuba.cx/txt/58911/38355484",
    })
    void testProxySources(int sourceId, String bookUrl, String chapterUrl) {
        this.bookUrl = bookUrl;
        this.chapterUrl = chapterUrl;

        config.setSourceId(sourceId);
        config.setProxyEnabled(1);
        config.setProxyHost("127.0.0.1");
        config.setProxyPort(7890);

        searchParse("辰东");
        bookParse();
        catalogParse();
        chapterParse();
    }

    public void searchParse(String keyword) {
        Console.log("\n{} START searchParse {}", DIVIDER, DIVIDER);
        List<SearchResult> parse = new SearchResultParser(config).parse(keyword);
        parse.forEach(System.out::println);
        Console.log("{} END searchParse {}\n", DIVIDER, DIVIDER);
    }

    public void bookParse() {
        Console.log("\n{} START bookParse {}", DIVIDER, DIVIDER);
        Book parse = new BookParser(config).parse(bookUrl);
        System.out.println(JSONUtil.toJsonPrettyStr(parse));
        Console.log("{} END bookParse {}\n", DIVIDER, DIVIDER);
    }

    public void catalogParse() {
        Console.log("\n{} START catalogParse {}", DIVIDER, DIVIDER);
        List<Chapter> parse = new CatalogParser(config).parse(bookUrl);
        parse.forEach(System.out::println);
        Console.log("{} END catalogParse {}\n", DIVIDER, DIVIDER);
    }

    public void chapterParse() {
        Console.log("\n{} START chapterParse {}", DIVIDER, DIVIDER);
        Chapter chapter = Chapter.builder().url(chapterUrl).build();
        Chapter beforeFiltration = new ChapterParser(config).parse(chapter);
        Chapter afterFiltration = new ChapterConverter(config).convert(beforeFiltration);
        System.out.println(afterFiltration.getContent());
        Console.log("{} END chapterParse {}\n", DIVIDER, DIVIDER);
    }

}