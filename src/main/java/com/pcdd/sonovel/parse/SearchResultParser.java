package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.ConfigBean;
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
    private ConfigBean config;

    public SearchResultParser(int sourceId) {
        super(sourceId);
    }

    public SearchResultParser(ConfigBean config) {
        super(config.getSourceId());
        this.config = config;
    }

    public List<SearchResult> parse(String keyword) {
        Rule.Search search = this.rule.getSearch();

        // 模拟搜索请求
        Document document;
        try {
            Connection.Response resp = getConn(search.getUrl())
                    .data(CrawlUtils.buildParams(this.rule.getSearch().getBody(), keyword))
                    .cookies(CrawlUtils.buildCookies(this.rule.getSearch().getCookies()))
                    .execute();
            document = resp.parse();
        } catch (Exception e) {
            Console.error(e.getMessage());
            return Collections.emptyList();
        }

        List<SearchResult> firstPageResults = getSearchResults(null, document);
        if (!search.getPagination()) return firstPageResults;

        Set<String> urls = new LinkedHashSet<>();
        for (Element e : document.select(search.getNextPage())) {
            String href = CrawlUtils.normalizeUrl(e.attr("href"), this.rule.getUrl());
            // TODO 中文解码，针对69书吧
            urls.add(URLUtil.decode(href));
        }

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
            document = getConn(url).get();

        Elements elements = document.select(rule.getResult());
        List<SearchResult> list = new ArrayList<>();
        for (Element element : elements) {
            // jsoup 不支持一次性获取属性的值
            String href = element.select(rule.getBookName()).attr("href");
            String bookName = element.select(rule.getBookName()).text();
            String author = element.select(rule.getAuthor()).text();
            String latestChapter = StrUtil.isNotEmpty(rule.getLatestChapter())
                    ? element.select(rule.getLatestChapter()).text()
                    : null;
            String update = StrUtil.isNotEmpty(rule.getUpdate())
                    ? element.select(rule.getUpdate()).text()
                    : null;

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

    private Connection getConn(String url) {
        Connection conn = Jsoup.connect(url)
                .method(CrawlUtils.buildMethod(this.rule.getSearch().getMethod()))
                .header("User-Agent", RandomUA.generate())
                .timeout(TIMEOUT_MILLS);

        // 启用配置文件的代理地址
        if (this.rule.isUseProxy()) {
            String proxyServer = config.getProxyServer();
            String[] split = proxyServer.split(":");
            conn.proxy(split[0], Integer.parseInt(split[1]));
        }

        return conn;
    }

}