package com.pcdd.sonovel.parse;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author pcdd
 */
public class BookParser extends Parser {

    public BookParser(int sourceId) {
        super(sourceId);
    }

    @SneakyThrows
    public Book parse(String url) {
        Rule.Book r = this.rule.getBook();
        Document document = Jsoup.parse(URLUtil.url(url), 30_000);
        String bookName = document.select(r.getBookName()).attr("content");
        String author = document.select(r.getAuthor()).attr("content");
        String description = document.select(r.getDescription()).attr("content");
        String coverUrl = document.select(r.getCoverUrl()).attr("src");

        Book book = new Book();
        book.setUrl(url);
        book.setBookName(bookName);
        book.setAuthor(author);
        book.setDescription(description);
        book.setCoverUrl(coverUrl);
        book.setCoverUrl(replaceCover(book));

        return book;
    }

    /**
     * 封面替换为起点最新封面
     */
    public static String replaceCover(Book book) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        headers.put("Cookie", "_yep_uuid=872f3c12-0048-d32c-9b9d-bfaadbd8e915; e2=%7B%22l6%22%3A%22%22%2C%22l1%22%3A3%2C%22pid%22%3A%22qd_P_Searchresult%22%2C%22eid%22%3A%22qd_S81%22%7D; e1=%7B%22l6%22%3A%22%22%2C%22l1%22%3A2%2C%22pid%22%3A%22qd_P_Searchresult%22%2C%22eid%22%3A%22%22%7D; _csrfToken=vON8Zm4iijH9gttLBBlj6ufd7bbOzndd6yCa9aVd; newstatisticUUID=1711462173_1460841294; fu=1288871991; Hm_lvt_f00f67093ce2f38f215010b699629083=1711462175; Hm_lpvt_f00f67093ce2f38f215010b699629083=1711462175; _gid=GA1.2.1155471355.1711462175; _ga=GA1.1.1993706799.1711462175; _ga_PFYW0QLV3P=GS1.1.1711460901.10.1.1711462174.0.0.0; traffic_utm_referer=; w_tsfp=ltvgWVEE2utBvS0Q6KvtkkmvETw7Z2R7xFw0D+M9Os09AacnUJyD145+vdfldCyCt5Mxutrd9MVxYnGAUtAnfxcSTciYb5tH1VPHx8NlntdKRQJtA5qJW1Qbd7J2umNBLW5YI0blj2ovIoFAybBoiVtZuyJ137ZlCa8hbMFbixsAqOPFm/97DxvSliPXAHGHM3wLc+6C6rgv8LlSgXyD8FmNOVlxdr9X0kCb1T0dC3FW9BO+AexINxmkKtutXZxDuDH2tz/iaJWl0QMh5FlBpRw4d9Lh2zC7JmNGJXkaewD23+I2Z7z6ZLh6+2xIAL5FW1kVqQ8ZteI5+URPDSi9YHWPBfp6tQAARvJZ/82seSvFxIb+c1AMu4Zt0AYlsYAN6DEjYTimKd8JSWTLNnUGfotRbsq+NHlkAkBbX2RE5Qdb; _ga_FZMMH98S83=GS1.1.1711460901.10.1.1711462266.0.0.0");

        HttpResponse resp = HttpRequest.get(StrUtil.format("https://www.qidian.com/so/{}.html", book.getBookName()))
                .headerMap(headers, true)
                .execute();

        Document document = Jsoup.parse(resp.body());
        resp.close();
        Elements elements = document.select(".res-book-item");

        for (Element e : elements) {
            String name = e.select(".book-mid-info > .book-info-title > a").text();
            String author = e.select(".book-mid-info > .author > .name").text();

            if (book.getBookName().equals(name) && book.getAuthor().equals(author)) {
                String coverUrl = e.select(".book-img-box > a > img").attr("src");
                return URLUtil.normalize(coverUrl).replace("/150", "");
            }
        }

        return book.getCoverUrl();
    }

}
