package com.pcdd.sonovel;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.script.ScriptUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.parse.CatalogParser;
import com.pcdd.sonovel.parse.ChapterParser;
import com.pcdd.sonovel.parse.SearchResultParser;
import com.pcdd.sonovel.util.ConfigUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author pcdd
 * Created at 2024/12/28
 */
class NewSourceTest {

    public static final AppConfig config = ConfigUtils.config();

    static {
        config.setSourceId(6);
    }

    @Test
    void crackSearch() {
        String kw = "辰东";
        String js = ResourceUtil.readUtf8Str("js/rule-6.js");
        Object key = ScriptUtil.invoke(js, "getParamB", kw);

        HttpResponse resp = HttpRequest
                .get("https://big5.quanben5.com/?c=book&a=search.json&callback=search&keywords=%s&b=%s".formatted(kw, key))
                .header("Referer", "https://big5.quanben5.com/search.html")
                .execute();
        String body = resp.body();
        String s = UnicodeUtil.toString(body);
        String s2 = HtmlUtil.unescape(s);
        String s3 = s2
                .replace("\\r", "")
                .replace("\\n", "")
                .replace("\\t", "")
                .replace("\\/", "/")
                .replace("\\\"", "'");
        String s4 = ReUtil.getGroup0("\\{(.*?)\\}", s3);
        JSONObject jsonObject = JSONUtil.parseObj(s4);
        String html = jsonObject.getStr("content");
        System.out.println(Jsoup.parse(html));
    }

    @Test
    void searchParse() {
        SearchResultParser sr = new SearchResultParser(config);
        List<SearchResult> parse = sr.parse("吞噬星空");
        parse.forEach(System.out::println);
    }

    @Test
    void bookParse() {
        BookParser bookParser = new BookParser(config);
        Book parse = bookParser.parse("https://69shux.co/book/15119.html");
        System.out.println(parse);
    }

    @Test
    void catalogParse() {
        CatalogParser catalogParser = new CatalogParser(config);
        List<Chapter> parse = catalogParser.parse("https://69shux.co/book/15119.html");
        parse.forEach(System.out::println);
    }

    @Test
    void chapterParse() {
        Chapter chapter = Chapter.builder()
                .url("https://69shux.co/txt/15119/7685425.html")
                .title("测试章节名")
                .build();
        ChapterParser chapterParser = new ChapterParser(config);
        System.out.println(chapterParser.parse(chapter));
    }

}