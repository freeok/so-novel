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
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.RandomUA;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * 获取小说最新封面工具类
 *
 * @author pcdd
 * Created at 2025/2/6
 */
@UtilityClass
public class CoverUpdater {

    private final AppConfig config = ConfigUtils.defaultConfig();
    private static final String DEFAULT_COVER = "https://bookcover.yuewen.com/qdbimg/no-cover";

    /**
     * 依次尝试不同来源获取封面
     */
    public String fetchCover(Book book, String coverUrl) {
        book.setCoverUrl(StrUtil.emptyToDefault(coverUrl, DEFAULT_COVER));

        if (StrUtil.isBlank(book.getBookName())) {
            return book.getCoverUrl();
        }

        return Stream.<Supplier<String>>of(
                        () -> fetchQidian(book),
                        () -> fetchZongheng(book),
                        () -> fetchChuangshi(book)
                )
                .map(Supplier::get)
                .filter(CoverUpdater::isValidCover)
                .findFirst()
                .orElse(book.getCoverUrl());
    }

    /**
     * 起点中文网
     */
    public String fetchQidian(Book book) {
        if (StrUtil.isEmpty(config.getQidianCookie())) return "";

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(Header.USER_AGENT.getValue(), RandomUA.generate());
        headers.put(Header.COOKIE.getValue(), config.getQidianCookie());

        String url = StrUtil.format("https://www.qidian.com/so/{}.html", book.getBookName());

        try (HttpResponse resp = HttpRequest.get(url).headerMap(headers, true).execute()) {
            Document document = Jsoup.parse(resp.body());

            for (Element e : document.select(".res-book-item")) {
                String qdName = e.select(".book-mid-info > .book-info-title > a").text();
                String qdAuthor1 = e.select(".book-mid-info > .author > .name").text();
                String qdAuthor2 = e.select(".book-mid-info > .author > i").text();
                String qdAuthor = StrUtil.emptyToDefault(qdAuthor1, qdAuthor2);

                if (matchBook(book, qdName, qdAuthor)) {
                    String cover = e.select(".book-img-box > a > img").attr("src");
                    return URLUtil.normalize(cover).replaceAll("/150(\\.webp)?", "");
                }
            }
        } catch (Exception e) {
            Console.error(e, render("获取起点封面失败：{}", "red"));
        }
        return "";
    }

    /**
     * 纵横中文网
     */
    public String fetchZongheng(Book book) {
        try (HttpResponse resp = HttpRequest.get("https://search.zongheng.com/search/book")
                .form(Map.of(
                        "keyword", book.getBookName(),
                        "pageNo", 1,
                        "pageNum", 20,
                        "isFromHuayu", 0
                ))
                .header(Header.USER_AGENT, RandomUA.generate())
                .execute()) {

            JSONObject datasField = JSONUtil.parseObj(resp.body())
                    .getJSONObject("data")
                    .getJSONObject("datas");

            if (datasField == null) {
                return "";
            }

            for (Object o : datasField.getJSONArray("list")) {
                JSONObject bookObj = (JSONObject) o;
                if (matchBook(book, bookObj.getStr("name"), bookObj.getStr("authorName"))) {
                    return "https://static.zongheng.com/upload" + bookObj.getStr("coverUrl");
                }
            }
        } catch (Exception e) {
            Console.error(e, render("获取纵横封面失败：{}", "red"));
        }
        return "";
    }

    /**
     * TODO 创世中文网
     */
    public String fetchChuangshi(Book book) {
        return "";
    }

    /**
     * 判断书名 + 作者是否匹配（忽略繁简体、HTML 标签）
     */
    private boolean matchBook(Book book, String name, String author) {
        String sourceName = HanLP.convertToSimplifiedChinese(book.getBookName());
        String sourceAuthor = HanLP.convertToSimplifiedChinese(book.getAuthor());
        name = HtmlUtil.cleanHtmlTag(name);
        author = HtmlUtil.cleanHtmlTag(author);
        return StrUtil.equals(sourceName, name) && StrUtil.equals(sourceAuthor, author);
    }

    /**
     * 封面 URL 校验
     */
    private boolean isValidCover(String coverUrl) {
        return StrUtil.isNotBlank(coverUrl) && Validator.isUrl(coverUrl);
    }

}