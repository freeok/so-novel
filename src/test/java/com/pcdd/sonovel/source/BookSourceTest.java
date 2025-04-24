package com.pcdd.sonovel.source;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import cn.hutool.system.SystemUtil;
import com.pcdd.sonovel.convert.ChapterConverter;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.*;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private List<Chapter> chapters;

    static {
        JsoupUtils.trustAllSSL();
        ConsoleLog.setLevel(Level.OFF);
        // 覆盖默认配置
        config.setLanguage("zh_CN");
        config.setExtName("txt");
        config.setThreads(-1);
    }

    @DisplayName("测试直连书源")
    @ParameterizedTest
    @CsvSource({
            "1, http://www.xbiqugu.la/130/130509/",
            "2, https://www.shuhaige.net/199178/",
            "3, http://www.mcxs.info/145_145199/",
            "4, http://www.99xs.info/tag/129_129843/",
            "5, https://www.tianxibook.com/book/66120/",
            "8, https://www.dxmwx.org/book/56441.html",
            "9, https://www.22biqu.com/biqu79148/",
            "10, http://www.xbiquzw.net/10_10233/",
            "11, https://www.0xs.net/txt/68398.html",
            "13, https://www.xbqg06.com/1582/",
            "14, https://www.luegeng.com/book186856/",
            "15, https://www.96dushu.com/book/344921/",
            "17, http://www.81zwwww.com/90_90170/",
            "18, http://www.ujxsw.net/book/107612/",
            "18, http://www.ujxsw.net/book/107612/",
            "19, http://www.yeudusk.com/book/1322535/",
    })
    void testDirectSources(int sourceId, String bookUrl) {
        this.bookUrl = bookUrl;
        config.setSourceId(sourceId);

        searchParse("zhttty");
        bookParse();
        tocParse();
        chapterParse();
        // chapterBatchParse(1, 850);
    }

    @DisplayName("测试代理书源")
    @ParameterizedTest
    @CsvSource({
            "6, https://quanben5.com/n/xinghedadi/",
            "7, https://www.69shuba.com/book/48273.htm",
            "12, https://www.deqixs.com/xiaoshuo/106/",
            "16, https://www.sudugu.com/1012/",
    })
    void testProxySources(int sourceId, String bookUrl) {
        this.bookUrl = bookUrl;
        config.setSourceId(sourceId);

        searchParse("zhttty");
        bookParse();
        tocParse();
        chapterParse();
    }

    public void searchParse(String keyword) {
        Console.log("\n{} START searchParse {}", DIVIDER, DIVIDER);
        List<SearchResult> list;
        if (config.getSourceId() == 6) {
            list = new SearchParser6(config).parse(keyword);
        } else {
            list = new SearchParser(config).parse(keyword, true);
        }
        if (CollUtil.isEmpty(list)) {
            Console.log("\"{}\"搜索结果为空", keyword);
        }
        if (!list.isEmpty()) {
            Console.log("点击此 URL 确保首条搜索结果访问有效: {}", CollUtil.getFirst(list).getUrl());
        }
        new SearchParser(config).printSearchResult(list);
        Console.log("{} END searchParse {}\n", DIVIDER, DIVIDER);
    }

    public void bookParse() {
        Console.log("\n{} START bookParse {}", DIVIDER, DIVIDER);
        Book book = new BookParser(config).parse(bookUrl);
        Console.log(JSONUtil.toJsonPrettyStr(book));
        Console.log("{} END bookParse {}\n", DIVIDER, DIVIDER);
    }

    public void tocParse() {
        Console.log("\n{} START tocParse {}", DIVIDER, DIVIDER);
        TocParser tocParser = new TocParser(config);
        List<Chapter> toc = tocParser.parse(bookUrl);
        chapters = toc;
        toc.forEach(System.out::println);
        if (CollUtil.isNotEmpty(toc)) {
            // 测试目录首章
            chapterUrl = toc.get(0).getUrl();
        } else {
            Console.log("目录为空");
        }
        Console.log("{} END tocParse {}\n", DIVIDER, DIVIDER);
    }

    public void chapterParse() {
        Console.log("\n{} START chapterParse {}", DIVIDER, DIVIDER);
        Chapter chapter = Chapter.builder().url(chapterUrl).build();
        Chapter beforeFiltration = new ChapterParser(config).parse(chapter);
        Chapter afterFiltration = new ChapterConverter(config).convert(beforeFiltration);
        Source source = new Source(config.getSourceId());
        ChineseConverter.convert(afterFiltration, source.rule.getLanguage(), config.getLanguage());
        Console.log(afterFiltration.getContent());
        Console.log("{} END chapterParse {}\n", DIVIDER, DIVIDER);
    }

    @SneakyThrows
    public void chapterBatchParse(int start, int end) {
        Console.log("\n{} START chapterBatchParse {}", DIVIDER, DIVIDER);
        ExecutorService threadPool = Executors.newFixedThreadPool(RuntimeUtil.getProcessorCount());
        CountDownLatch latch = new CountDownLatch(end - start);
        Source source = new Source(config.getSourceId());

        for (Chapter chapter : CollUtil.sub(chapters, start, end)) {
            threadPool.execute(() -> {
                Chapter o = Chapter.builder().url(chapter.getUrl()).build();
                Chapter beforeFiltration = new ChapterParser(config).parse(o);
                Chapter afterFiltration = new ChapterConverter(config).convert(beforeFiltration);
                ChineseConverter.convert(afterFiltration, source.rule.getLanguage(), config.getLanguage());
                Console.log("✅ " + o.getTitle());
                latch.countDown();
            });
        }

        latch.await();
        threadPool.shutdown();
        Console.log("{} END chapterBatchParse {}\n", DIVIDER, DIVIDER);
    }

}