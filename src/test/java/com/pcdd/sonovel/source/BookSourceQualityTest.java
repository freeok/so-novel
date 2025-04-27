package com.pcdd.sonovel.source;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.SearchParser;
import com.pcdd.sonovel.parse.SearchParser6;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.OkHttpUtils;
import com.pcdd.sonovel.util.RandomUA;
import com.pcdd.sonovel.util.SourceUtils;
import lombok.Data;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 根据起点榜单测试书源质量，结果写入 Markdown
 *
 * @author pcdd
 * Created at 2024/12/5
 */
class BookSourceQualityTest {

    static final AppConfig config = ConfigUtils.config();
    static final Map<String, List<Book>> ranks = new ConcurrentHashMap<>();
    // 测试排行榜前几名 (0,20]
    static final int TOP_NUM = 20;
    // 1：封IP，6：老书，12、16：限流
    static final String RE_SKIP_IDS = "1|6|12|16";

    static {
        ConsoleLog.setLevel(Level.OFF);
        config.setLanguage("zh_CN");
    }

    void qidianRankInit(Map<String, String> map) {
        for (Map.Entry<String, String> kv : map.entrySet()) {
            ranks.put(kv.getKey(), getQiDianRanks(kv.getValue()));
        }
    }

    List<Book> getQiDianRanks(String rankUrl) {
        Console.log("getQiDianRanks: {}", rankUrl);
        List<Book> rank = new ArrayList<>();
        Document document = null;
        try (Response resp = OkHttpUtils.createClient()
                .newCall(new Request.Builder()
                        .url(rankUrl)
                        .addHeader(Header.USER_AGENT.getValue(), RandomUA.generate())
                        .addHeader(Header.COOKIE.getValue(), "w_tsfp=ltvuV0MF2utBvS0Q6qPpnE2sFzsidD04h0wpEaR0f5thQLErU5mG2IZyuMn2NHDf6sxnvd7DsZoyJTLYCJI3dwMSRpqReokRhQ/ElYgnjtxAVBI1QJzYWAJJJLly7DdAf3hCNxS00jA8eIUd379yilkMsyN1zap3TO14fstJ019E6KDQmI5uDW3HlFWQRzaLbjcMcuqPr6g18L5a5TjetFupeV8iA+sXhU3B3HlKWC4gskCyIuAJNBmlI5j5SqA=")
                        .build()
                )
                .execute()) {
            document = Jsoup.parse(resp.body().string());
        } catch (IOException e) {
            Console.error(e);
        }

        Elements elements = document.select("#book-img-text > ul > li");
        // 取前 N 名
        for (Element e : elements.subList(0, TOP_NUM)) {
            String url = URLUtil.normalize(e.select("div.book-mid-info > h2 > a").attr("href"));
            String bookName = e.select("div.book-mid-info > h2 > a").text();
            String author = e.select("div.book-mid-info > p.author > a.name").text();

            Book book = new Book();
            book.setBookName(bookName);
            book.setAuthor(author);
            book.setUrl(url);

            rank.add(book);
        }

        return rank;
    }

    /**
     * 测试统计，前 20 名
     * test count: 11
     * thread: 6
     * search interval: 500 ~ 1000 ms
     * 4 m 41 s
     * <p>
     */
    @Test
    void test() {
        int count = SourceUtils.getCount();
        // 生成的 markdown 文件
        Map<String, String> map = Map.of(
                "1-起点月票榜", "https://www.qidian.com/rank/yuepiao/",
                "2-起点畅销榜", "https://www.qidian.com/rank/hotsales/",
                "3-起点阅读指数榜", "https://www.qidian.com/rank/readIndex/",
                "4-起点推荐榜·月榜", "https://www.qidian.com/rank/recom/datetype2/",
                "5-起点收藏榜", "https://www.qidian.com/rank/collect/",
                "6-起点签约作者新书榜", "https://www.qidian.com/rank/signnewbook/"
        );
        qidianRankInit(map);

        String divider = "-".repeat(50);
        ExecutorService threadPool = Executors.newFixedThreadPool(map.size());

        try {
            // 遍历榜单
            for (Map.Entry<String, String> kv : map.entrySet()) {
                threadPool.execute(() -> {
                    Console.log("{} {} {}", divider, kv.getKey(), divider);
                    Map<Integer, List<SourceQuality>> sourceQualityListMap = new HashMap<>();

                    // 遍历书源
                    for (int id = 1; id <= count; id++) {
                        Rule rule = new Source(id).rule;
                        // 跳过书源：不支持搜索的、搜索有限流的、搜索意义不大的、暂时无法访问的
                        if (rule.getSearch() != null && !String.valueOf(rule.getId()).matches(RE_SKIP_IDS)) {
                            sourceQualityListMap.put(id, getSourceQualityList(id, kv));
                        }
                    }

                    generateMarkdown(kv.getKey(), sourceQualityListMap);
                });
            }
        } catch (Exception e) {
            Console.log(e.getMessage());
        } finally {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, TimeUnit.MINUTES)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    List<SourceQuality> getSourceQualityList(int id, Map.Entry<String, String> rank) {
        int foundCount = 0;
        int notFoundCount = 0;
        List<SourceQuality> list = new ArrayList<>();
        Rule rule = new Source(id).rule;

        String divider = "=".repeat(30);
        Console.log("{} 测试书源质量 {} | 书源 {} {} ({}) {}",
                divider, rank.getKey(), rule.getId(), rule.getUrl(), rule.getName(), divider);
        config.setSourceId(id);
        // 需要代理的书源
        config.setProxyEnabled(rule.isNeedProxy() ? 1 : 0);
        SearchParser sp = new SearchParser(config);
        SearchParser6 sp6 = new SearchParser6(config);

        // 遍历 20 本，即搜索源站 20 次，注意爬取频率
        for (Book b : ranks.get(rank.getKey())) {
            SourceQuality sq = new SourceQuality();
            sq.setSourceId(rule.getId());
            sq.setBookName(b.getBookName());
            sq.setAuthor(b.getAuthor());
            sq.setQiDianUrl(b.getUrl());

            List<SearchResult> results;
            if (config.getSourceId() == 6) {
                results = sp6.parse(b.getBookName());
            } else {
                results = sp.parse(b.getBookName());
            }
            boolean found = false;
            for (SearchResult r : results) {
                if (Objects.equals(r.getBookName(), b.getBookName()) && Objects.equals(r.getAuthor(), b.getAuthor())) {
                    Console.log("书源 {} 已找到《{}》（{}）{}\t{}\t{}",
                            id, r.getBookName(), r.getAuthor(), rank.getKey(), r.getUrl(), b.getUrl());
                    sq.setUrl(r.getUrl());
                    found = true;
                    foundCount++;
                    break;
                }
            }
            if (!found) {
                Console.log("书源 {} 未找到《{}》（{}）{}\t{}",
                        id, b.getBookName(), b.getAuthor(), rank.getKey(), b.getUrl());
                notFoundCount++;
            }
            sq.setFound(found);
            list.add(sq);

            // 针对搜索限流书源的处理
            /* int randomInt = RandomUtil.randomInt(200, 500);
            Console.log("搜索间隔 {} ms", randomInt);
            try {
                Thread.sleep(randomInt);
            } catch (InterruptedException e) {
                Console.error(e);
            } */
        }
        Console.log("书源 {} ({})，{}已找到 {} 本，未找到 {} 本\n\n",
                rule.getId(), rule.getUrl(), rank.getKey(), foundCount, notFoundCount);

        return list;
    }

    void generateMarkdown(String title, Map<Integer, List<SourceQuality>> map) {
        if (MapUtil.isEmpty(map)) {
            Console.error("{} sourceQualityListMap 为空", title);
            return;
        }

        String fileName = "qidian_rank/%s.md".formatted(title);
        Console.log("<== generateMarkdown: {}", fileName);
        // 表头
        StringBuilder sourceNameCol = new StringBuilder("|");
        // 分隔线
        StringBuilder dividerCol = new StringBuilder("|");

        for (Integer id : map.keySet()) {
            sourceNameCol.append(" 书源 ").append(id).append(" |");
            dividerCol.append(" ---- |");
        }

        StringBuilder md = new StringBuilder();
        // # 起点xx榜前 TOP_NUM (yyyy-MM-dd)
        md.append(StrUtil.format("{}前 {} 名 ({})\n",
                "# " + title,
                TOP_NUM,
                DateTime.now().toString(DatePattern.NORM_DATE_PATTERN)));
        md.append(StrUtil.format("| 排名 | 书名 | 作者 {} 起点链接 |\n", sourceNameCol));
        md.append(StrUtil.format("| ---- | ---- | ---- {} ---- |\n", dividerCol));

        // 获取 map 任意非空元素的 value
        List<SourceQuality> list = CollUtil.getFirst(CollUtil.filter(map.values(), Objects::nonNull));
        for (int i = 0; i < list.size(); i++) {
            SourceQuality o = list.get(i);
            StringBuilder foundBuilder = new StringBuilder();

            for (List<SourceQuality> item : map.values()) {
                SourceQuality sq = item.get(i);
                foundBuilder.append(StrUtil.format("{} |", sq.getFound() ? "✅" : "❌"));
            }

            md.append(StrUtil.format("| {} | {} | {} | {} {} |\n",
                    i + 1,
                    o.getBookName(),
                    o.getAuthor(),
                    foundBuilder,
                    o.getQiDianUrl()));
        }

        try {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            writer.write(md.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Console.error(e);
        }
    }

}

@Data
class SourceQuality {
    Integer sourceId;
    String bookName;
    String author;
    Boolean found;
    String url;
    String qiDianUrl;
}