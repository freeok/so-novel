package com.pcdd.sonovel.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.parse.CatalogParser;
import com.pcdd.sonovel.parse.ChapterParser;
import com.pcdd.sonovel.parse.SearchResultParser;
import com.pcdd.sonovel.util.Settings;
import lombok.SneakyThrows;

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
    private static final String EXT_NAME;
    private static final String SAVE_PATH;
    private static final int THREADS;
    private static String bookDir;

    // 加载配置文件参数
    static {
        Props sys = Settings.sys();
        Props usr = Settings.usr();

        SOURCE_ID = sys.getInt("source_id");

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
     * 爬取小说
     *
     * @param list  搜索到的小说列表
     * @param num   下载序号
     * @param start 从第几章下载
     * @param end   下载到第几章
     */
    @SneakyThrows
    public static double crawl(List<SearchResult> list, int num, int start, int end) {
        SearchResult r = list.get(num);
        // 小说详情页url
        String url = r.getUrl();
        String bookName = r.getBookName();
        String author = r.getAuthor();
        Book book = new BookParser(SOURCE_ID).parse(url);

        // 小说目录名格式：书名(作者)
        bookDir = String.format("%s (%s)", bookName, author);
        // 必须 new File()，否则无法使用 . 和 ..
        File dir = FileUtil.mkdir(new File(SAVE_PATH + File.separator + bookDir));
        if (!dir.exists()) {
            // C:\Program Files 下创建需要管理员权限
            Console.log(render("@|red 创建下载目录失败\n1. 检查下载路径是否合法\n2. 尝试以管理员身份运行（C 盘部分目录需要管理员权限）|@"));
            return 0;
        }

        // 获取小说目录
        CatalogParser catalogParser = new CatalogParser(SOURCE_ID);
        List<Chapter> catalog = catalogParser.parse(url, start, end);
        // 防止 start、end 超出范围
        if (CollUtil.isEmpty(catalog)) {
            Console.log(render(StrUtil.format("@|yellow 超出章节范围，该小说共 {} 章|@", catalogParser.parse(url).size())));
            return 0;
        }

        int autoThreads = Runtime.getRuntime().availableProcessors() * 2;
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(THREADS == -1 ? autoThreads : THREADS);
        // 阻塞主线程，用于计时
        CountDownLatch countDownLatch = new CountDownLatch(catalog.size());

        Console.log("<== 开始下载《{}》（{}） 共计 {} 章 | 线程数：{}", bookName, author, catalog.size(), autoThreads);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ChapterParser chapterParser = new ChapterParser(SOURCE_ID);
        // 爬取章节并下载
        catalog.forEach(item -> executor.execute(() -> {
            Chapter chapter = chapterParser.parse(item, countDownLatch);
            download(chapter, countDownLatch);
            countDownLatch.countDown();
        }));
        // 等待全部下载完毕
        countDownLatch.await();
        executor.shutdown();

        CrawlerPostHandler.handle(EXT_NAME, book, dir);

        stopWatch.stop();
        return stopWatch.getTotalTimeSeconds();
    }

    /**
     * 下载章节
     */
    private static void download(Chapter chapter, CountDownLatch latch) {
        if (chapter == null) {
            return;
        }
        // epub 格式转换前为 html
        String extName = Objects.equals("epub", EXT_NAME) ? "html" : EXT_NAME;
        String parentPath = SAVE_PATH + File.separator + bookDir + File.separator;
        String path = switch (EXT_NAME) {
            case "html" -> parentPath + chapter.getChapterNo() + "_." + extName;
            default -> parentPath + chapter.getChapterNo()
                    // Windows 文件名非法字符替换
                    + "_" + chapter.getTitle().replaceAll("[\\\\/:*?<>]", "")
                    + "." + extName;
        };
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(path))) {
            fos.write(chapter.getContent().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            latch.countDown();
            Console.error(e, e.getMessage());
        }
    }

}
