package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.RandomUA;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * @author pcdd
 * Created at 2024/3/23
 */
public class SearchResultParser extends Source {

    private static final int TIMEOUT_MILLS = 15_000;

    public SearchResultParser(int sourceId) {
        super(sourceId);
    }

    public List<SearchResult> parse(String keyword) {
        Rule.Search search = this.rule.getSearch();
        boolean isPaging = search.getPagination();

        // 模拟搜索请求
        Document document;
        try {
            Connection.Response resp = Jsoup.connect(search.getUrl())
                    .method(CrawlUtils.buildMethod(this.rule.getSearch().getMethod()))
                    .timeout(TIMEOUT_MILLS)
                    .header("User-Agent", RandomUA.generate())
                    .data(CrawlUtils.buildParams(this.rule.getSearch().getBody(), keyword))
                    .cookies(CrawlUtils.buildCookies(this.rule.getSearch().getCookies()))
                    .execute();
            document = resp.parse();
        } catch (Exception e) {
            Console.error(e.getMessage());
            return Collections.emptyList();
        }

        List<SearchResult> firstPageResults = getSearchResults(null, document);
        if (!isPaging) return firstPageResults;

        Set<String> urls = new LinkedHashSet<>();
        for (Element e : document.select(search.getNextPage()))
            urls.add(CrawlUtils.normalizeUrl(e.attr("href"), this.rule.getUrl()));

        // 使用并行流处理分页 URL
        List<SearchResult> additionalResults = urls.parallelStream()
                .flatMap(url -> getSearchResults(url, null).stream())
                .toList();

        // 合并，不去重（去重用 union）
        return CollUtil.unionAll(firstPageResults, additionalResults);
    }

    @SneakyThrows
    private List<SearchResult> getSearchResults(String url, Document document) {
        Rule.Search rule = this.rule.getSearch();
        // 搜索结果页 DOM
        if (document == null)
            document = Jsoup.connect(url).timeout(TIMEOUT_MILLS).get();

        Elements elements = document.select(rule.getResult());
        List<SearchResult> list = new ArrayList<>();
        for (Element element : elements) {
            // jsoup 不支持一次性获取属性的值
            String href = element.select(rule.getBookName()).attr("href");
            String bookName = element.select(rule.getBookName()).text();
            String latestChapter = element.select(rule.getLatestChapter()).text();
            String author = element.select(rule.getAuthor()).text();
            String update = element.select(rule.getUpdate()).text();

            // 针对书源 1：排除第一个 tr 表头
            if (bookName.isEmpty()) continue;

            SearchResult build = SearchResult.builder()
                    .url(CrawlUtils.normalizeUrl(href, this.rule.getUrl()))
                    .bookName(bookName)
                    .latestChapter(latestChapter)
                    .author(author)
                    .latestUpdate(update)
                    .build();

            list.add(build);
        }

        return list;
    }

}