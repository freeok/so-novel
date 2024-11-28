package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
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
        boolean isPaging = search.getPagination();

        // 模拟搜索请求
        Connection.Response resp = Jsoup.connect(search.getUrl())
                .method(buildMethod())
                .timeout(TIMEOUT_MILLS)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5410.0 Safari/537.36")
                .data(buildParams(keyword))
                .cookies(buildCookies())
                .execute();
        Document document = resp.parse();

        List<SearchResult> firstPageResults = getSearchResults(null, document);
        if (!isPaging) return firstPageResults;

        Set<String> urls = new LinkedHashSet<>();
        for (Element e : document.select(search.getNextPage()))
            urls.add(buildUrl(e.attr("href")));

        // 使用并行流处理分页 URL
        List<SearchResult> additionalResults = urls.parallelStream()
                .flatMap(url -> getSearchResults(url, null).stream())
                .toList();

        // 合并，不去重（去重用 union）
        return CollUtil.unionAll(firstPageResults, additionalResults);
    }

    @SneakyThrows
    private List<SearchResult> getSearchResults(String url, Document document) {
        Rule.Search search = this.rule.getSearch();
        // 搜索结果页 DOM
        if (document == null)
            document = Jsoup.connect(url).timeout(TIMEOUT_MILLS).get();

        Elements elements = document.select(search.getResult());

        List<SearchResult> list = new ArrayList<>();
        for (Element element : elements) {
            // jsoup 不支持一次性获取属性的值
            String href = element.select(search.getBookName()).attr("href");
            String bookName = element.select(search.getBookName()).text();
            String latestChapter = element.select(search.getLatestChapter()).text();
            String author = element.select(search.getAuthor()).text();
            String update = element.select(search.getUpdate()).text();

            // 如果存在任何一个字符串为空字符串（针对书源 1：排除第一个 tr 表头）
            if (Stream.of(href, bookName, latestChapter, author, update).anyMatch(String::isEmpty)) continue;

            SearchResult build = SearchResult.builder()
                    .url(buildUrl(href))
                    .bookName(bookName)
                    .latestChapter(latestChapter)
                    .author(author)
                    .latestUpdate(update)
                    .build();

            list.add(build);
        }

        return list;
    }

    // 有的 href 是相对路径，需要拼接为完整路径
    private String buildUrl(String href) {
        return Validator.isUrl(href) ? href : URLUtil.normalize(this.rule.getUrl() + href);
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

    private Connection.Method buildMethod() {
        String method = this.rule.getSearch().getMethod().toLowerCase();

        return switch (method) {
            case "get" -> Connection.Method.GET;
            case "post" -> Connection.Method.POST;
            case "put" -> Connection.Method.PUT;
            case "delete" -> Connection.Method.DELETE;
            case "patch" -> Connection.Method.PATCH;
            case "head" -> Connection.Method.HEAD;
            case "options" -> Connection.Method.OPTIONS;
            case "trace" -> Connection.Method.TRACE;
            default -> Connection.Method.POST;
        };
    }

}
