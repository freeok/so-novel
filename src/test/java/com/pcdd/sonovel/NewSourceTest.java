package com.pcdd.sonovel;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.script.ScriptUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.*;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.parse.CatalogParser;
import com.pcdd.sonovel.parse.ChapterParser;
import com.pcdd.sonovel.parse.SearchResultParser;
import com.pcdd.sonovel.util.ConfigUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        String kw = "我吃西红柿";
        String js = ResourceUtil.readUtf8Str("js/rule-6.js");
        Object key = ScriptUtil.invoke(js, "getParamB", kw);

        Source source = new Source(6);
        Rule.Search ruleSearch = source.rule.getSearch();
        String param = ruleSearch.getData().formatted(key, kw);
        /* JSONObject obj = JSONUtil.parseObj(param);
        System.out.println(JSONUtil.toJsonPrettyStr(obj)); */

        Map<String, String> map = JSONUtil.toBean(param, Map.class);
        HttpRequest req = HttpRequest
                .get(ruleSearch.getUrl())
                .header("Referer", ruleSearch.getUrl() + "search.html")
                .formStr(map);

        System.out.println(req.form());
        String body = req.execute().body();
        String s = UnicodeUtil.toString(body);
        String s2 = HtmlUtil.unescape(s)
                .replace("\\r", "")
                .replace("\\n", "")
                .replace("\\t", "")
                .replace("\\/", "/")
                .replace("\\\"", "'");
        String s4 = ReUtil.getGroup0("\\{(.*?)\\}", s2);
        JSONObject jsonObject = JSONUtil.parseObj(s4);
        String html = jsonObject.getStr("content");
        System.out.println(Jsoup.parse(html));
    }

    @Test
    void searchParse() {
        SearchResultParser sr = new SearchResultParser(config);
        List<SearchResult> parse = sr.parse("斗罗大陆");
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