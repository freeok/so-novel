package com.pcdd.sonovel.core;

import cn.hutool.core.img.ImgUtil;
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
import com.pcdd.sonovel.util.RandomUA;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * 获取小说最新封面工具类
 *
 * @author pcdd
 * Created at 2025/2/6
 */
@UtilityClass
public class CoverUpdater {

    private final int TIMEOUT = 3000;
    private final String DEFAULT_COVER = "https://bookcover.yuewen.com/qdbimg/no-cover";
    private final AppConfig APP_CONFIG = AppConfigLoader.APP_CONFIG;

    /**
     * 从不同来源获取最新封面
     */
    public String fetchCover(Book book, String coverUrl) {
        Console.log("<== 开始获取最新封面");
        book.setCoverUrl(StrUtil.emptyToDefault(coverUrl, DEFAULT_COVER));

        if (StrUtil.isBlank(book.getBookName())) {
            return book.getCoverUrl();
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<String>> tasks = List.of(
                    () -> fetchQidian(book),
                    () -> fetchZongheng(book),
                    () -> fetchQimao(book)
            );

            // 等待所有任务完成
            List<Future<String>> futures = executor.invokeAll(tasks);
            // 封面 url，分辨率大小
            Map<String, Integer> map = new HashMap<>();
            for (Future<String> future : futures) {
                String url = future.get();
                if (isValidCover(url)) {
                    BufferedImage img = ImgUtil.read(URLUtil.url(url));
                    map.put(url, img.getWidth() * img.getHeight());
                }
            }

            // 返回分辨率最高的封面，若不存在有效封面，则使用默认封面
            return map.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(book.getCoverUrl());

        } catch (Exception e) {
            Console.error(e, "并行获取封面失败");
        }

        return book.getCoverUrl();
    }

    /**
     * 起点中文网
     */
    public String fetchQidian(Book book) {
        if (StrUtil.isEmpty(APP_CONFIG.getQidianCookie())) return "";
        String url = StrUtil.format("https://www.qidian.com/so/{}.html", book.getBookName());
        try (HttpResponse resp = HttpRequest.get(url)
                .headerMap(Map.of(
                        Header.USER_AGENT.getValue(), RandomUA.generate(),
                        Header.COOKIE.getValue(), APP_CONFIG.getQidianCookie()
                ), true)
                .timeout(TIMEOUT)
                .execute()) {
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
                .timeout(TIMEOUT)
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
     * 七猫小说网
     */
    public String fetchQimao(Book book) {
        try (HttpResponse resp = HttpRequest.get("https://www.qimao.com/qimaoapi/api/search/result")
                .form(Map.of(
                        "keyword", book.getBookName(),
                        "count", 0,
                        "page", 1,
                        "page_size", 15
                ))
                .header(Header.USER_AGENT, RandomUA.generate())
                .timeout(TIMEOUT)
                .execute()) {

            JSONObject datasField = JSONUtil.parseObj(resp.body()).getJSONObject("data");
            if (datasField == null) {
                return "";
            }

            for (Object o : datasField.getJSONArray("search_list")) {
                JSONObject bookObj = (JSONObject) o;
                if (matchBook(book, bookObj.getStr("title"), bookObj.getStr("author"))) {
                    return bookObj.getStr("image_link");
                }
            }
        } catch (Exception e) {
            Console.error(e, render("获取七猫封面失败：{}", "red"));
        }
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