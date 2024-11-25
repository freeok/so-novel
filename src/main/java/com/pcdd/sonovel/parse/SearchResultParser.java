package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author pcdd
 */
public class SearchResultParser extends Source {

    private static final int TIMEOUT_MILLS = 15_000;

    public SearchResultParser(int sourceId) {
        super(sourceId);
    }

    @SneakyThrows
    public List<SearchResult> parse(String keyword) {
        Rule.Search search = this.rule.getSearch();
        // 搜索结果页 DOM
        Document document = Jsoup.connect(search.getUrl())
                .timeout(TIMEOUT_MILLS)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5410.0 Safari/537.36")
                .data(buildParams(keyword))
                .cookies(buildCookies())
                .post();
        Elements elements = document.select(search.getResult());

        List<SearchResult> list = new ArrayList<>();
        for (Element element : elements) {
            // jsoup 不支持一次性获取属性的值
            String href = element.select(search.getBookName()).attr("href");
            String bookName = element.select(search.getBookName()).text();
            String latestChapter = element.select(search.getLatestChapter()).text();
            String author = element.select(search.getAuthor()).text();
            String update = element.select(search.getUpdate()).text();

            // 针对书源 1：排除第一个 tr（表头）
            // 如果存在任何一个字符串为空字符串，则执行相应的操作
            if (Stream.of(href, bookName, latestChapter, author, update).anyMatch(String::isEmpty)) continue;

            // 有的 href 是相对路径，需要拼接为完整路径
            href = Validator.isUrl(href) ? href : URLUtil.normalize(this.rule.getUrl() + href);

            SearchResult build = SearchResult.builder()
                    .url(href)
                    .bookName(bookName)
                    .latestChapter(latestChapter)
                    .author(author)
                    .latestUpdate(update)
                    .build();

            list.add(build);
        }

        return list;
    }

    private Map<String, String> buildParams(String keyword) {
        Map<String, String> params = new HashMap<>();

        JSONUtil.parseObj(this.rule.getSearch().getBody())
                .forEach((key, value) -> {
                    if (key.equals("kw")) params.put(value.toString(), keyword);
                    else params.put(key, value.toString());
                });

        return params;
    }

    private Map<String, String> buildCookies() {
        Map<String, String> cookies = new HashMap<>();

        JSONUtil.parseObj(this.rule.getSearch().getCookies())
                .forEach((key, value) -> cookies.put(key, value.toString()));

        return cookies;
    }


}
