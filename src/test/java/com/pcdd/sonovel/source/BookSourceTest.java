package com.pcdd.sonovel.source;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.convert.ChapterConverter;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.OkHttpClientFactory;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.*;
import com.pcdd.sonovel.util.ConfigUtils;
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

    private static final AppConfig config = ConfigUtils.defaultConfig();
    private static final String DIVIDER = "=".repeat(50);
    private String bookUrl;
    private String firstChapterUrl;
    private List<Chapter> chapters;

    static {
        HttpClientContext.set(OkHttpClientFactory.create(config, true));
        ConsoleLog.setLevel(Level.OFF);
        // 覆盖默认配置
        config.setExtName("txt");
        config.setLanguage("zh_TW");
    }

    @DisplayName("测试直连书源")
    @ParameterizedTest
    @CsvSource({
            "1, http://www.xbiqugu.la/130/130509/",
            "2, https://www.shuhaige.net/199178/",
            "3, http://www.mcxs.info/145_145199/",
            "4, http://www.99xs.info/tag/129_129843/",
            "5, https://www.tianxibook.com/book/66120/",
            "6, https://www.dxmwx.org/book/56441.html",
            "7, https://www.22biqu.com/biqu79148/",
            "8, http://www.xbiquzw.net/10_10233/",
            "9, https://www.0xs.net/txt/68398.html",
            "10, https://www.xshbook.com/0/94328173/",
            "11, https://www.luegeng.com/book186856/",
            "12, http://www.shu009.com/book/111616/",
            "13, http://www.81zwwww.com/90_90170/",
            "14, http://www.ujxsw.org/book/107612/",
            "15, http://www.yeudusk.com/book/1322535/",
            "16, https://www.wxsy.net/novel/1803/",
            "17, http://www.xhytd.com/32/32957/",
            "18, https://www.laoyaoxs.org/info/281469.html"
    })
    void testDirectSources(int sourceId, String bookUrl) {
        this.bookUrl = bookUrl;
        config.setSourceId(sourceId);

        searchParse("耳根");
        bookParse();
        tocParse(1, Integer.MAX_VALUE);
        chapterParse();
        // chapterBatchParse(0, 100);
    }

    @DisplayName("测试代理书源")
    @ParameterizedTest
    @CsvSource({
            "1, https://www.69shuba.com/book/48273.htm",
            "2, https://quanben5.com/n/henchunhenaimei/",
            "3, https://www.deqixs.com/xiaoshuo/106/",
            "4, https://www.sudugu.com/1012/",
    })
    void testProxySources(int sourceId, String bookUrl) {
        this.bookUrl = bookUrl;
        config.setSourceId(sourceId);

        searchParse("耳根");
        bookParse();
        tocParse(1, Integer.MAX_VALUE);
        chapterParse();
    }

    public void searchParse(String keyword) {
        Console.log("\n{} START searchParse {}", DIVIDER, DIVIDER);
        List<SearchResult> list;
        if ("proxy-rules.json".equals(config.getActiveRules()) && config.getSourceId() == 2) {
            list = new SearchParserQuanben5(config).parse(keyword);
        } else {
            list = new SearchParser(config).parse(keyword, true);
        }
        if (CollUtil.isEmpty(list)) {
            Console.log("\"{}\"搜索结果为空", keyword);
        }
        if (!list.isEmpty()) {
            Console.log("点击此 URL 检查首条搜索结果有效性: {}", CollUtil.getFirst(list).getUrl());
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

    public void tocParse(int start, int end) {
        Console.log("\n{} START tocParse {}", DIVIDER, DIVIDER);
        TocParser tocParser = new TocParser(config);
        List<Chapter> toc = tocParser.parse(bookUrl, start, end);
        chapters = toc;
        toc.forEach(System.out::println);
        if (CollUtil.isNotEmpty(toc)) {
            // 测试目录首章
            firstChapterUrl = toc.get(0).getUrl();
        } else {
            Console.log("目录为空");
        }
        Console.log("{} END tocParse {}\n", DIVIDER, DIVIDER);
    }

    public void chapterParse() {
        Console.log("\n{} START chapterParse {}", DIVIDER, DIVIDER);

        Chapter build = Chapter.builder().url(firstChapterUrl).build();
        Chapter chapter = new ChapterParser(config).parse(build);

        Console.log(chapter.getContent());
        Console.log("{} END chapterParse {}\n", DIVIDER, DIVIDER);
    }

    /**
     * 测试章节是否限流
     */
    @SneakyThrows
    public void chapterBatchParse(int start, int end) {
        Console.log("\n{} START chapterBatchParse {}", DIVIDER, DIVIDER);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ExecutorService threadPool = Executors.newFixedThreadPool(RuntimeUtil.getProcessorCount() * 5);
        CountDownLatch latch = new CountDownLatch(end - start);
        Source source = new Source(config.getSourceId());

        for (Chapter chapter : CollUtil.sub(chapters, start, end)) {
            threadPool.execute(() -> {
                Chapter o = Chapter.builder().url(chapter.getUrl()).build();
                Chapter beforeFiltration = new ChapterParser(config).parse(o);
                Chapter afterFiltration = new ChapterConverter(config).convert(beforeFiltration);
                Chapter res = ChineseConverter.convert(afterFiltration, source.rule.getLanguage(), config.getLanguage());
                if (StrUtil.isAllNotEmpty(res.getTitle(), res.getContent())) {
                    Console.log("✅ {}", res.getTitle());
                } else {
                    StringBuilder errMsg = new StringBuilder("❌ %s %s ".formatted(res.getTitle(), res.getUrl()));
                    if (StrUtil.isEmpty(res.getTitle())) {
                        errMsg.append("章节标题为空 ");
                    }
                    if (StrUtil.isEmpty(res.getContent())) {
                        errMsg.append("章节正文为空");
                    }
                    Console.log(errMsg);
                }

                latch.countDown();
            });
        }

        latch.await();
        threadPool.shutdown();
        stopWatch.stop();

        Console.log("总耗时 {} s", NumberUtil.round(stopWatch.getTotalTimeSeconds(), 2));
        Console.log("{} END chapterBatchParse {}\n", DIVIDER, DIVIDER);
    }

}