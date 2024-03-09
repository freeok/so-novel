package com.pcdd.sonovel.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import com.pcdd.sonovel.Main;
import com.pcdd.sonovel.model.SearchResult;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * @author pcdd
 * Created at 2021/6/10 17:03
 */
public class SearchNovelUtils {

    private static String indexUrl;
    private static String searchUrl;
    private static String savePath;
    private static String extName;
    private static String novelDir;
    private static long minTimeInterval;
    private static long maxTimeInterval;

    // 加载配置文件，初始化参数
    static {
        Properties p = new Properties();
        InputStream is = Main.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            p.load(is);
            indexUrl = p.get("index_url").toString();
            searchUrl = p.get("search_url").toString();
            savePath = p.get("savePath").toString();
            extName = p.get("extName").toString();
            minTimeInterval = Convert.toLong(p.get("min"), 0L);
            maxTimeInterval = Convert.toLong(p.get("max"), 1L);
        } catch (IOException e) {
            Console.error("初始化参数失败：" + e.getMessage());
        }
    }

    private SearchNovelUtils() {
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
        long start = System.currentTimeMillis();
        Connection connect = Jsoup.connect(searchUrl);
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

        Console.log("<== 搜索到 {} 条记录，耗时 {} s\n",
                elements.size(),
                NumberUtil.round((System.currentTimeMillis() - start) / 1000.0, 2)
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
        File file = new File(savePath + novelDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        Document document = Jsoup.parse(new URL(url), 10000);
        // 获取小说目录
        Elements elements = document.getElementById("list").getElementsByTag("a");
        Console.log("==> 开始下载：《{}》（{}）", bookName, author);

        // 线程池
        ExecutorService executor = Executors.newFixedThreadPool(1);
        // 阻塞主线程，用于计时
        CountDownLatch countDownLatch = new CountDownLatch(end == Integer.MAX_VALUE ? elements.size() : end);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // elements.size()是小说的总章数
        for (int i = start - 1; i < end && i < elements.size(); i++) {
            int finalI = i;
            executor.execute(() -> {
                String title = elements.get(finalI).text();
                String chapterUrl = indexUrl + elements.get(finalI).attr("href");
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
        long timeInterval = ThreadLocalRandom.current().nextLong(minTimeInterval, maxTimeInterval);
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
        String path = savePath + novelDir + File.separator
                + chapterNo + "_" + chapterName
                + "." + extName;
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(path))) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

}
