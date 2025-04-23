package com.pcdd.sonovel.core;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hankcs.hanlp.HanLP;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.RandomUA;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.jline.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2025/2/6
 */
@UtilityClass
public class CoverUpdater {

    /**
     * 依次尝试不同来源获取封面
     */
    public String fetchCover(Book book, String coverUrl) {
        // 无封面，使用默认封面
        book.setCoverUrl(StrUtil.isEmpty(coverUrl) ? "https://bookcover.yuewen.com/qdbimg/no-cover" : coverUrl);
        if (StrUtil.isEmpty(book.getBookName())) {
            return book.getCoverUrl();
        }
        return Stream.<Supplier<String>>of(
                        () -> fetchQidian(book),
                        () -> fetchZongheng(book),
                        () -> fetchChuangshi(book)
                ).map(Supplier::get)
                .filter(CoverUpdater::isValidCover)
                .findFirst()
                .orElse(book.getCoverUrl());
    }

    /**
     * 起点中文网
     */
    public String fetchQidian(Book book) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(Header.USER_AGENT.getValue(), RandomUA.generate());
        headers.put(Header.COOKIE.getValue(), "w_tsfp=ltvgWVEE2utBvS0Q6KvtkkmvETw7Z2R7xFw0D+M9Os09AacnUJyD145+vdfldCyCt5Mxutrd9MVxYnGAUtAnfxcSTciYb5tH1VPHx8NlntdKRQJtA5qJW1Qbd7J2umNBLW5YI0blj2ovIoFAybBoiVtZuyJ137ZlCa8hbMFbixsAqOPFm/97DxvSliPXAHGHM3wLc+6C6rgv8LlSgXyD8FmNOVlxdr9X0kCb1T0dC3FW9BO+AexINxmkKtutXZxDuDH2tz/iaJWl0QMh5FlBpRw4d9Lh2zC7JmNGJXkaewD23+I2Z7z6ZLh6+2xIAL5FW1kVqQ8ZteI5+URPDSi9YHWPBfp6tQAARvJZ/82seSvFxIb+c1AMu4Zt0AYlsYAN6DEjYTimKd8JSWTLNnUGfotRbsq+NHlkAkBbX2RE5Qdb;");

        try {
            HttpResponse resp = HttpRequest.get(StrUtil.format("https://www.qidian.com/so/{}.html", book.getBookName()))
                    .headerMap(headers, true)
                    .execute();
            Document document = Jsoup.parse(resp.body());
            resp.close();

            for (Element e : document.select(".res-book-item")) {
                String qdName = e.select(".book-mid-info > .book-info-title > a").text();
                // 起点作家
                String qdAuthor1 = e.select(".book-mid-info > .author > .name").text();
                // 非起点作家
                String qdAuthor2 = e.select(".book-mid-info > .author > i").text();
                String qdAuthor = StrUtil.isEmpty(qdAuthor1) ? qdAuthor2 : qdAuthor1;

                if (matchBook(book, qdName, qdAuthor)) {
                    return URLUtil.normalize(e.select(".book-img-box > a > img").attr("src"))
                            .replaceAll("/150(\\.webp)?", "");
                }
            }
        } catch (Exception e) {
            Console.error(render("获取起点封面失败：{}", e.getMessage(), "red"));
        }
        return null;
    }

    /**
     * 纵横中文网
     */
    public String fetchZongheng(Book book) {
        try {
            String url = "https://search.zongheng.com/search/book";
            // 自动拼接查询字符串
            Map<String, Object> params = new HashMap<>();
            params.put("keyword", book.getBookName());
            params.put("pageNo", 1);
            params.put("pageNum", 20);
            params.put("isFromHuayu", 0);

            HttpRequest req = HttpRequest.get(url)
                    .form(params)
                    .header(Header.USER_AGENT, RandomUA.generate());
            HttpResponse resp = req.execute();
            String body = resp.body();
            JSONObject datasField = JSONUtil.parseObj(body)
                    .getJSONObject("data")
                    .getJSONObject("datas");

            // 搜索结果未空
            if (datasField == null) {
                return null;
            }

            for (Object o : datasField.getJSONArray("list")) {
                JSONObject bookObj = (JSONObject) o;
                if (matchBook(book, bookObj.getStr("name"), bookObj.getStr("authorName"))) {
                    return "https://static.zongheng.com/upload" + bookObj.getStr("coverUrl");
                }
            }

            resp.close();
        } catch (Exception e) {
            Console.error(e);
            return null;
        }

        return null;
    }

    /**
     * 创世中文网
     */
    public String fetchChuangshi(Book book) {
        return null;
    }

    private boolean matchBook(Book book, String name, String author) {
        String sourceName = HanLP.convertToSimplifiedChinese(book.getBookName());
        String sourceAuthor = HanLP.convertToSimplifiedChinese(book.getAuthor());
        name = HtmlUtil.cleanHtmlTag(name);
        author = HtmlUtil.cleanHtmlTag(author);
        return StrUtil.equals(sourceName, name) && StrUtil.equals(sourceAuthor, author);
    }

    private boolean isValidCover(String coverUrl) {
        return StrUtil.isNotBlank(coverUrl) && Validator.isUrl(coverUrl);
    }

}