package work.pcdd.sonovel.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import work.pcdd.sonovel.Application;
import work.pcdd.sonovel.bean.SearchResultLine;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author 1907263405@qq.com
 * @date 2021/6/10 17:03
 */
public class SearchNovelUtils {

    private static String indexUrl;
    private static String searchUrl = null;
    private static String savePath = null;
    private static String novelDir = null;
    private static long min;
    private static long max;

    // 加载配置文件，初始化参数
    static {
        Properties pro = new Properties();
        InputStream is = Application.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            pro.load(is);
            indexUrl = pro.get("index_url").toString();
            searchUrl = pro.get("search_url").toString();
            savePath = pro.get("savePath").toString();
            min = Convert.toLong(pro.get("min"), 50L);
            max = Convert.toLong(pro.get("max"), 100L);
        } catch (IOException e) {
            Console.log("初始化参数失败：" + e.getMessage());
            e.printStackTrace();
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
    public static List<SearchResultLine> search(String keyword) {
        Console.log("==> 正在搜索...");
        long start = System.currentTimeMillis();
        Connection connect = Jsoup.connect(searchUrl);
        Document document = null;
        try {
            // 搜索结果页DOM
            document = connect.data("searchkey", keyword).post();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // tr:nth-child(n+2)表示获取第2个tr开始获取
        Elements elements = document.select("#checkform > table > tbody > tr:nth-child(n+2)");

        List<SearchResultLine> list = new ArrayList<>();
        for (Element element : elements) {
            SearchResultLine searchResultLine = SearchResultLine.builder()
                    .bookName(element.child(0).text())
                    .author(element.child(2).text())
                    .latestChapter(element.child(1).text())
                    .latestUpdate(element.child(3).text())
                    .link(element.child(0).getElementsByAttribute("href").attr("href"))
                    .build();
            list.add(searchResultLine);
        }

        Console.log("<== 搜索到{}条记录，耗时{}s",
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
    public static double crawl(List<SearchResultLine> list, int num, int start, int end) {
        try {
            SearchResultLine searchResultLine = list.get(num);
            // 小说名
            String bookName = searchResultLine.getBookName();
            // 小说链接
            String url = searchResultLine.getLink();
            Console.log("==> 开始下载：《{}》", bookName);
            // 设置小说目录名
            novelDir = bookName;
            File file = new File(savePath + bookName);
            if (!file.exists()) {
                file.mkdirs();
            }
            Document document = Jsoup.parse(new URL(url), 10000);
            // 获取小说目录
            Elements elements = document.getElementById("list").getElementsByTag("a");
            Elements info = document.getElementById("info").getElementsByTag("p");
            // 获取小说作者
            String author = info.eq(0).text().substring(info.eq(0).text().indexOf(":") + 1);
            // 获取小说类别
            String category = info.eq(1).text().substring(info.eq(1).text().indexOf(":") + 1);
            // 获取最后更新
            String last = info.eq(2).text().substring(info.eq(2).text().indexOf(":") + 1);
            Console.log(bookName + " " + url + " " + author + " " + category + " " + last);

            long startTime = System.currentTimeMillis();
            // elements.size()是小说的总章数
            for (int i = start - 1; i < end && i < elements.size(); i++) {
                String title = elements.get(i).text();
                String href = indexUrl + elements.get(i).attr("href");
                crawlChapter(i + 1 + "_" + title, href);
            }
            return (System.currentTimeMillis() - startTime) / 1000.0;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 爬取具体页面的正文
     *
     * @param title 标题
     * @param url   链接
     */
    @SneakyThrows
    private static void crawlChapter(String title, String url) {
        // 设置时间间隔
        long timeInterval = ThreadLocalRandom.current().nextLong(min, max);
        TimeUnit.MILLISECONDS.sleep(timeInterval);
        Console.log("{} 正在下载：{} 间隔{}ms", Thread.currentThread().getName(), title, timeInterval);
        Document document = Jsoup.parse(new URL(url), 10000);
        String content = document.getElementById("content").html();
        download(title, content);
    }

    @SneakyThrows
    private static void download(String title, String content) {
        String path = savePath + novelDir + File.separator + title + ".html";
        OutputStream fos = new BufferedOutputStream(new FileOutputStream(path));
        fos.write(content.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

}
