package com.pcdd.sonovel;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.SearchResultParser;
import com.pcdd.sonovel.util.RandomUA;
import lombok.Data;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author pcdd
 * Created at 2024/12/5
 */
public class BookSourceQualityTest {

    public static void main(String[] args) {
        // 测试前几个书源
        int count = 4;
        Map<String, String> map = new LinkedHashMap<>();
        map.put("起点月票榜", "https://www.qidian.com/rank/yuepiao/");
        map.put("起点畅销榜", "https://www.qidian.com/rank/hotsales/");
        map.put("起点阅读指数榜", "https://www.qidian.com/rank/readIndex/");
        map.put("起点推荐榜月榜", "https://www.qidian.com/rank/recom/datetype2/");
        map.put("起点收藏榜", "https://www.qidian.com/rank/collect/");
        map.put("起点签约作者新书榜", "https://www.qidian.com/rank/signnewbook/");
        map.put("起点月票榜·VIP新作", "https://www.qidian.com/rank/yuepiao/chn0/");

        String divider = "-".repeat(50);
        for (Map.Entry<String, String> kv : map.entrySet()) {
            Console.log("{} {} {}", divider, kv.getKey(), divider);
            List<List<SourceQuality>> lists = new ArrayList<>();

            for (int i = 1; i <= count; i++) {
                lists.add(getSourceQualityList(i, kv.getValue()));
            }

            generateMarkdown("# " + kv.getKey(), lists, kv.getKey() + ".md");
        }
    }

    @SneakyThrows
    static void generateMarkdown(String name, List<List<SourceQuality>> lists, String fileName) {
        Console.log("<== 开始生成 {}", fileName);
        // 表头
        StringBuilder s1 = new StringBuilder("|");
        // 分隔线
        StringBuilder s2 = new StringBuilder("|");
        for (int i = 1; i <= lists.size(); i++) {
            s1.append(" 书源 ").append(i).append(" |");
            s2.append(" ---- |");
        }

        StringBuilder result = new StringBuilder();
        // # 起点月票榜前 20 (2024-12-05)
        result.append(StrUtil.format("{}前 {} ({})\n",
                name,
                lists.get(0).size(),
                DateTime.now().toString(DatePattern.NORM_DATE_PATTERN)));
        result.append(StrUtil.format("| 排名 | 书名 | 作者 {} 起点链接 |\n", s1));
        result.append(StrUtil.format("| ---- | ---- | ---- {} ---- |\n", s2));

        List<SourceQuality> list = lists.get(0);

        for (int i = 0; i < list.size(); i++) {
            SourceQuality o = list.get(i);

            StringBuilder foundBuilder = new StringBuilder();
            for (List<SourceQuality> item : lists) {
                SourceQuality sq = item.get(i);
                foundBuilder.append(StrUtil.format("{} |", sq.getFound() ? "✅" : "❌"));
            }

            result.append(StrUtil.format("| {} | {} | {} | {} {} |\n",
                    i + 1,
                    o.getBookName(),
                    o.getAuthor(),
                    foundBuilder,
                    o.getQiDianUrl()));
        }

        Writer writer = new FileWriter(fileName);
        writer.write(result.toString());
        writer.flush();
        writer.close();
    }

    @SneakyThrows
    static List<Book> getQiDianRanks(String rankUrl) {
        List<Book> ranks = new ArrayList<>();
        // 月票榜
        Document document = Jsoup.connect(rankUrl).timeout(5000).header(Header.USER_AGENT.getValue(), RandomUA.generate()).header(Header.COOKIE.getValue(), "newstatisticUUID=1733353201_1218475646; _csrfToken=xCa4yKqsUG8xYxk6G8B3NZv9VFWJg8ooKtSX5sls; fu=1131660441; Hm_lvt_f00f67093ce2f38f215010b699629083=1733353200; Hm_lpvt_f00f67093ce2f38f215010b699629083=1733353200; HMACCOUNT=98F935B166DF50B4; traffic_utm_referer=https%3A//github.com/freeok/so-novel/issues/new; _gid=GA1.2.296158232.1733353201; _gat_gtag_UA_199934072_2=1; _ga_FZMMH98S83=GS1.1.1733353200.49.1.1733353200.0.0.0; _ga=GA1.1.1897847021.1733353201; _ga_PFYW0QLV3P=GS1.1.1733353200.49.1.1733353200.0.0.0; w_tsfp=ltvuV0MF2utBvS0Q6qnqkUisFjkmfDE4h0wpEaR0f5thQLErU5mG0oV/vcn2MnLY5Mxnvd7DsZoyJTLYCJI3dwMGWd7AIddOzlDBzpMvzo4UARBnEZvcWVcfI7t17DBGfGpeJUXmjG9+JdRBzbVgmEUe4HsgnvE0CbBqdNlK0wkX4PXSnNtpWWiWnFKZQDTPdnYNLerYpr93+K5S9i2R").get();

        Elements elements = document.select("#book-img-text > ul > li");

        for (Element e : elements) {
            String url = URLUtil.normalize(e.select("div.book-mid-info > h2 > a").attr("href"));
            String bookName = e.select("div.book-mid-info > h2 > a").text();
            String author = e.select("div.book-mid-info > p.author > a.name").text();

            Book book = new Book();
            book.setBookName(bookName);
            book.setAuthor(author);
            book.setUrl(url);

            ranks.add(book);
        }

        return ranks;
    }

    @SneakyThrows
    static List<SourceQuality> getSourceQualityList(int sourceId, String rankUrl) {
        int foundCount = 0;
        int notFoundCount = 0;
        List<SourceQuality> list = new ArrayList<>();
        Rule rule = new Source(sourceId).rule;

        Console.log("<== 开始测试书源质量：书源 {} {} ({})", rule.getId(), rule.getUrl(), rule.getName());

        for (Book b : getQiDianRanks(rankUrl)) {
            SourceQuality sq = new SourceQuality();
            sq.setSourceId(rule.getId());
            sq.setBookName(b.getBookName());
            sq.setAuthor(b.getAuthor());
            sq.setQiDianUrl(b.getUrl());

            List<SearchResult> results = new SearchResultParser(sourceId).parse(b.getBookName());
            // 针对书源 4 author 会包含“作者：”的情况
            for (SearchResult sr : results) {
                sr.setAuthor(sr.getAuthor().replace("作者：", ""));
            }
            boolean found = false;

            for (SearchResult r : results) {
                if (Objects.equals(r.getBookName(), b.getBookName()) && Objects.equals(r.getAuthor(), b.getAuthor())) {
                    Console.log("已找到 《{}》（{}）\t{}\t{}", r.getBookName(), r.getAuthor(), r.getUrl(), b.getUrl());
                    sq.setUrl(r.getUrl());
                    found = true;
                    foundCount++;
                    break;
                }
            }
            if (!found) {
                Console.log("未找到 《{}》（{}）\t{}", b.getBookName(), b.getAuthor(), b.getUrl());
                notFoundCount++;
            }
            sq.setFound(found);
            list.add(sq);
        }
        Console.log("书源 {} ({})，已找到 {} 本，未找到 {} 本\n", rule.getId(), rule.getUrl(), foundCount, notFoundCount);

        return list;
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