package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.RandomUA;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jline.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
public class BookParser6 extends Source {

    private static final int TIMEOUT_MILLS = 15_000;

    public BookParser6(AppConfig config) {
        super(config);
    }

    @SneakyThrows
    public Book parse(String url) {
        Rule.Book r = this.rule.getBook();
        Document document = jsoupConn(url, TIMEOUT_MILLS).get();
        String bookName = document.select(r.getBookName()).text();
        String author = document.select(r.getAuthor()).text();
        String intro = document.select(r.getIntro()).text();
        intro = StrUtil.cleanBlank(intro);
        String coverUrl = document.select(r.getCoverUrl()).attr("src");

        Book book = new Book();
        book.setUrl(url);
        book.setBookName(bookName);
        book.setAuthor(author);
        book.setIntro(intro);
        book.setCoverUrl(CrawlUtils.normalizeUrl(coverUrl, this.rule.getUrl()));
        book.setCoverUrl(replaceCover(book));

        return ChineseConverter.convert(book, this.rule.getLanguage(), config.getLanguage());
    }

    /**
     * 封面替换为起点最新封面
     */
    public static String replaceCover(Book book) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(Header.USER_AGENT.getValue(), RandomUA.generate());
        headers.put(Header.COOKIE.getValue(), "w_tsfp=ltvgWVEE2utBvS0Q6KvtkkmvETw7Z2R7xFw0D+M9Os09AacnUJyD145+vdfldCyCt5Mxutrd9MVxYnGAUtAnfxcSTciYb5tH1VPHx8NlntdKRQJtA5qJW1Qbd7J2umNBLW5YI0blj2ovIoFAybBoiVtZuyJ137ZlCa8hbMFbixsAqOPFm/97DxvSliPXAHGHM3wLc+6C6rgv8LlSgXyD8FmNOVlxdr9X0kCb1T0dC3FW9BO+AexINxmkKtutXZxDuDH2tz/iaJWl0QMh5FlBpRw4d9Lh2zC7JmNGJXkaewD23+I2Z7z6ZLh6+2xIAL5FW1kVqQ8ZteI5+URPDSi9YHWPBfp6tQAARvJZ/82seSvFxIb+c1AMu4Zt0AYlsYAN6DEjYTimKd8JSWTLNnUGfotRbsq+NHlkAkBbX2RE5Qdb;");
        HttpResponse resp = HttpRequest.get(StrUtil.format("https://www.qidian.com/so/{}.html", book.getBookName()))
                .headerMap(headers, true)
                .execute();

        Document document = Jsoup.parse(resp.body());
        resp.close();
        Elements elements = document.select(".res-book-item");

        try {
            for (Element e : elements) {
                String qdName = e.select(".book-mid-info > .book-info-title > a").text();
                // 起点作家
                String qdAuthor1 = e.select(".book-mid-info > .author > .name").text();
                // 非起点作家
                String qdAuthor2 = e.select(".book-mid-info > .author > i").text();
                String qdAuthor = StrUtil.isEmpty(qdAuthor1) ? qdAuthor2 : qdAuthor1;

                String name = HanLP.convertToSimplifiedChinese(book.getBookName());
                String author = HanLP.convertToSimplifiedChinese(book.getAuthor());

                if (name.equals(qdName) && author.equals(qdAuthor)) {
                    String coverUrl = e.select(".book-img-box > a > img").attr("src");
                    // 替换为高清原图
                    return URLUtil.normalize(coverUrl).replaceAll("/150(\\.webp)?", "");
                }
            }
        } catch (Exception e) {
            Console.error(render("最新封面获取失败：{}", e.getMessage()));
            return book.getCoverUrl();
        }

        return book.getCoverUrl();
    }

}