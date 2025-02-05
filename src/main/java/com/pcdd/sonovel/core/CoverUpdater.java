package com.pcdd.sonovel.core;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.RandomUA;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jline.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2025/2/6
 */
@UtilityClass
public class CoverUpdater {

    /**
     * 封面替换为起点最新封面
     */
    public String fetchQidian(Book book) {
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