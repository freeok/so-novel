package com.pcdd.sonovel.core;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.parse.ChapterParser;
import com.pcdd.sonovel.parse.SearchResultParser;
import com.pcdd.sonovel.util.Settings;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2021/6/10 17:03
 */
public class Crawler {

    private static final int SOURCE_ID;
    private static final String INDEX_URL;
    private static final String EXT_NAME;
    private static final String SAVE_PATH;
    private static final int THREADS;
    private static String bookDir;

    // 加载配置文件参数
    static {
        Props sys = Settings.sys();
        Props usr = Settings.usr();

        SOURCE_ID = sys.getInt("source_id");
        INDEX_URL = sys.getStr("index_url");

        EXT_NAME = usr.getStr("extName");
        SAVE_PATH = usr.getStr("savePath");
        THREADS = usr.getInt("threads");
    }

    private Crawler() {
    }

    /**
     * 搜索小说
     *
     * @param keyword 关键字
     * @return 匹配的小说列表
     */
    @SneakyThrows
    public static List<SearchResult> search(String keyword) {
        Console.log("<== 正在搜索...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        SearchResultParser searchResultParser = new SearchResultParser(SOURCE_ID);
        List<SearchResult> searchResults = searchResultParser.parse(keyword);

        stopWatch.stop();
        Console.log("<== 搜索到 {} 条记录，耗时 {} s\n",
                searchResults.size(),
                NumberUtil.round(stopWatch.getTotalTimeSeconds(), 2)
        );

        return searchResults;
    }

    /**
     * 爬取小说 TODO 解耦
     *
     * @param list  搜索到的小说列表
     * @param num   下载序号
     * @param start 从第几章下载
     * @param end   下载到第几章
     */
    @SneakyThrows
    public static double crawl(List<SearchResult> list, int num, int start, int end) {
        SearchResult r = list.get(num);
        String bookName = r.getBookName();
        String author = r.getAuthor();
        // 小说详情页url
        String url = r.getUrl();

        // 小说目录名格式：书名(作者)
        bookDir = String.format("%s (%s)", bookName, author);
        File dir = FileUtil.mkdir(new File(SAVE_PATH + File.separator + bookDir));
        if (!dir.exists()) {
            // C:\Program Files 下创建需要管理员权限
            Console.log(render("@|red 创建下载目录失败，安装目录需要管理员权限|@"));
            return 0;
        }

        Book book = new BookParser(SOURCE_ID).parse(url);
        Document document = Jsoup.parse(URLUtil.url(url), 30_000);
        // 获取小说目录 TODO 抽取为 CatalogParser
        Elements elements = document.getElementById("list").getElementsByTag("a");
        int autoThreads = Runtime.getRuntime().availableProcessors() * 2;
        // 线程池
        ExecutorService executor = Executors.newFixedThreadPool(THREADS == -1 ? autoThreads : THREADS);
        // 阻塞主线程，用于计时
        CountDownLatch countDownLatch = new CountDownLatch(end == Integer.MAX_VALUE ? elements.size() : end);

        Console.log("<== 开始下载《{}》（{}） 共计 {} 章 | 线程数：{}", bookName, author, elements.size(), autoThreads);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ChapterParser chapterParser = new ChapterParser(SOURCE_ID);
        // 爬取章节并下载
        for (int i = start - 1; i < end && i < elements.size(); i++) {
            int finalI = i;
            executor.execute(() -> {
                Chapter build = Chapter.builder()
                        .chapterNo(finalI + 1)
                        .title(elements.get(finalI).text())
                        .url(INDEX_URL + elements.get(finalI).attr("href"))
                        .build();
                Chapter parse = chapterParser.parse(build, countDownLatch);
                download(parse, countDownLatch);
                countDownLatch.countDown();
            });
        }
        // 等待全部下载完毕
        countDownLatch.await();
        executor.shutdown();

        CrawlerPostHandler.handle(EXT_NAME, book, dir);

        stopWatch.stop();
        return stopWatch.getTotalTimeSeconds();
    }

    /**
     * 下载到本地
     */
    private static void download(Chapter chapter, CountDownLatch latch) {
        // epub 格式转换前为 html
        String extName = Objects.equals("epub", EXT_NAME) ? "html" : EXT_NAME;
        String path = SAVE_PATH + File.separator + bookDir + File.separator
                + chapter.getChapterNo()
                // Windows 文件名非法字符替换
                + "_" + chapter.getTitle().replaceAll("[\\\\/:*?<>]", "")
                + "." + extName;
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(path))) {
            fos.write(chapter.getContent().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            latch.countDown();
            Console.error(e, e.getMessage());
        }
    }

}
