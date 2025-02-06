package com.pcdd.sonovel.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.handle.CrawlerPostHandler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.*;
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
 * Created at 2021/6/10
 */
public class Crawler {

    private final AppConfig config;
    private String bookDir;

    public Crawler(AppConfig config) {
        this.config = config;
    }

    /**
     * 搜索小说
     *
     * @param keyword 关键字
     * @return 匹配的小说列表
     */
    public List<SearchResult> search(String keyword) {
        Console.log("<== 正在搜索...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<SearchResult> searchResults;

        if (config.getSourceId() == 6) {
            searchResults = new SearchResultParser6(config).parse(keyword);
        } else {
            searchResults = new SearchResultParser(config).parse(keyword);
        }

        stopWatch.stop();
        Console.log("<== 搜索到 {} 条记录，耗时 {} s", searchResults.size(), NumberUtil.round(stopWatch.getTotalTimeSeconds(), 2));

        return searchResults;
    }

    /**
     * 爬取小说
     *
     * @param sr  小说详情
     * @param toc 小说目录
     */
    @SneakyThrows
    public double crawl(SearchResult sr, List<Chapter> toc) {
        // 小说详情页url
        String url = sr.getUrl();
        String bookName = sr.getBookName();
        String author = sr.getAuthor();
        Book book;

        // 根据 css selector 解析
        if (config.getSourceId() == 6) {
            book = new BookParser6(config).parse(url);
        } else { // 根据 meta 解析
            book = new BookParser(config).parse(url);
        }

        // 小说目录名格式：书名(作者)
        bookDir = String.format("%s (%s)", bookName, author);
        // 必须 new File()，否则无法使用 . 和 ..
        File dir = FileUtil.mkdir(new File(config.getDownloadPath() + File.separator + bookDir));
        if (!dir.exists()) {
            // C:\Program Files 下创建需要管理员权限
            Console.log(render("@|red 创建下载目录失败\n1. 检查下载路径是否合法\n2. 尝试以管理员身份运行（C 盘部分目录需要管理员权限）|@"));
            return 0;
        }

        // 防止 start、end 超出范围
        if (CollUtil.isEmpty(toc)) {
            Console.log(render(StrUtil.format("@|yellow 超出章节范围，该小说共 {} 章|@", toc.size())));
            return 0;
        }

        int autoThreads = config.getThreads() == -1 ? Runtime.getRuntime().availableProcessors() * 2 : config.getThreads();
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(autoThreads);
        // 阻塞主线程，用于计时
        CountDownLatch latch = new CountDownLatch(toc.size());

        Console.log("<== 开始下载《{}》（{}） 共计 {} 章 | 线程数：{}", bookName, author, toc.size(), autoThreads);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ChapterParser chapterParser = new ChapterParser(config);
        // 下载章节
        toc.forEach(item -> executor.execute(() -> {
            createChapterFile(chapterParser.parse(item, latch, sr));
            Console.log("<== 待下载章节数：{}", latch.getCount());
        }));

        // 阻塞主线程，等待章节全部下载完毕
        latch.await();
        executor.shutdown();
        new CrawlerPostHandler(config).handle(book, dir);
        stopWatch.stop();

        return stopWatch.getTotalTimeSeconds();
    }

    /**
     * 保存章节
     */
    private void createChapterFile(Chapter chapter) {
        if (chapter == null) return;

        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(generatePath(chapter)))) {
            fos.write(chapter.getContent().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Console.error(e, e.getMessage());
        }
    }

    private String generatePath(Chapter chapter) {
        // epub 格式转换前的格式为 html
        String extName = Objects.equals("epub", config.getExtName()) ? "html" : config.getExtName();
        String parentPath = config.getDownloadPath() + File.separator + bookDir + File.separator;
        return switch (config.getExtName()) {
            case "html" -> parentPath + chapter.getOrder() + "_." + extName;
            case "epub", "txt" -> parentPath + chapter.getOrder()
                    // Windows 文件名非法字符替换
                    + "_" + chapter.getTitle().replaceAll("[\\\\/:*?<>]", "") + "." + extName;
            default -> throw new IllegalStateException("暂不支持的下载格式: " + config.getExtName());
        };
    }

}