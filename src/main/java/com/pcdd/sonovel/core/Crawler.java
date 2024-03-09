package com.pcdd.sonovel.core;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.SearchResult;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author pcdd
 * Created at 2021/6/10 17:03
 */
public class Crawler {

    private static final String INDEX_URL;
    private static final String SEARCH_URL;
    private static final String EXT_NAME;
    private static final String SAVE_PATH;
    private static String novelDir;
    private static final int THREADS;
    private static final long MIN_TIME_INTERVAL;
    private static final long MAX_TIME_INTERVAL;

    // 加载配置文件参数
    static {
        Props p = Props.getProp("config.properties", StandardCharsets.UTF_8);
        INDEX_URL = p.getStr("index_url");
        SEARCH_URL = p.getStr("search_url");
        EXT_NAME = p.getStr("extName");
        SAVE_PATH = p.getStr("savePath");
        THREADS = p.getInt("threads");
        MIN_TIME_INTERVAL = p.getLong("min");
        MAX_TIME_INTERVAL = p.getLong("max");
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
        Console.log("==> 正在搜索...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Connection connect = Jsoup.connect(SEARCH_URL);
        // 搜索结果页DOM
        Document document = connect.data("searchkey", keyword).post();

        // tr:nth-child(n+2)表示获取第2个tr开始获取
        Elements elements = document.select("#checkform > table > tbody > tr:nth-child(n+2)");

        List<SearchResult> list = new ArrayList<>();
        for (Element element : elements) {
            SearchResult searchResult = SearchResult.builder()
                    .bookName(element.child(0).text())
                    .author(element.child(2).text())
                    .latestChapter(element.child(1).text())
                    .latestUpdate(element.child(3).text())
                    .url(element.child(0).getElementsByAttribute("href").attr("href"))
                    .build();
            list.add(searchResult);
        }

        stopWatch.stop();
        Console.log("<== 搜索到 {} 条记录，耗时 {} s\n",
                elements.size(),
                NumberUtil.round(stopWatch.getTotalTimeSeconds(), 2)
        );

        return list;
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
        String bookName = r.getBookName();
        String author = r.getAuthor();
        // 小说详情页url
        String url = r.getUrl();

        // 小说目录名格式：书名(作者)
        novelDir = String.format("%s（%s）", bookName, author);
        File file = new File(SAVE_PATH + novelDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        Document document = Jsoup.parse(new URL(url), 10000);
        // 获取小说目录
        Elements elements = document.getElementById("list").getElementsByTag("a");
        Console.log("==> 开始下载：《{}》（{}）", bookName, author);

        // 线程池
        ExecutorService executor = Executors.newFixedThreadPool(THREADS == -1
                ? Runtime.getRuntime().availableProcessors() * 2
                : THREADS);
        // 阻塞主线程，用于计时
        CountDownLatch countDownLatch = new CountDownLatch(end == Integer.MAX_VALUE ? elements.size() : end);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // elements.size()是小说的总章数
        for (int i = start - 1; i < end && i < elements.size(); i++) {
            int finalI = i;
            executor.execute(() -> {
                String title = elements.get(finalI).text();
                String chapterUrl = INDEX_URL + elements.get(finalI).attr("href");
                crawlChapter(finalI + 1, title, chapterUrl);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        stopWatch.stop();

        return stopWatch.getTotalTimeSeconds();
    }

    /**
     * 爬取小说章节
     *
     * @param chapterNo   章节序号
     * @param chapterName 章节名
     * @param chapterUrl  小说正文页 url
     */
    @SneakyThrows
    private static void crawlChapter(int chapterNo, String chapterName, String chapterUrl) {
        // 设置时间间隔
        long timeInterval = ThreadLocalRandom.current().nextLong(MIN_TIME_INTERVAL, MAX_TIME_INTERVAL);
        TimeUnit.MILLISECONDS.sleep(timeInterval);
        Console.log("正在下载：【{}】 间隔 {} ms", chapterName, timeInterval);
        Document document = Jsoup.parse(new URL(chapterUrl), 10000);
        String content = document.getElementById("content").html();
        download(chapterNo, chapterName, content);
    }

    /**
     * 下载到本地
     *
     * @param chapterNo   章节序号
     * @param chapterName 章节名
     * @param content     正文
     */
    @SneakyThrows
    private static void download(int chapterNo, String chapterName, String content) {
        String path = SAVE_PATH + novelDir + File.separator
                + chapterNo + "_" + chapterName
                + "." + EXT_NAME;
        // TODO fix 载过快时报错：Exception in thread "pool-2-thread-10" java.io.FileNotFoundException: \so-novel-download\史上最强炼气期（李道然）\3141_第三千一百三十二章 万劫不复 为无敌妙妙琪的两顶皇冠加更（2\2）.html (系统找不到指定的路径。)
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(path))) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

}
