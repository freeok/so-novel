package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.handle.SearchResultsHandler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.CrawlUtils;
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

    private static final int TIMEOUT_MILLS = 10_000;

    public SearchResultParser(AppConfig config) {
        super(config);
    }

    public SearchResultParser(int sourceId) {
        super(sourceId, null);
    }

    public List<SearchResult> parse(String keyword) {
        // 模拟搜索请求
        Document document;
        Connection.Response resp;
        Rule.Search search = this.rule.getSearch();
        try {
            resp = jsoupConn(search.getUrl(), TIMEOUT_MILLS)
                    .data(CrawlUtils.buildParams(this.rule.getSearch().getBody(), keyword))
                    .cookies(CrawlUtils.buildCookies(this.rule.getSearch().getCookies()))
                    .execute();
            document = Jsoup.parse(resp.body());
        } catch (Exception e) {
            Console.error(e.getMessage());
            return Collections.emptyList();
        }

        List<SearchResult> firstPageResults = getSearchResults(null, resp);
        if (!search.isPagination()) return SearchResultsHandler.handle(firstPageResults);

        Set<String> urls = new LinkedHashSet<>();
        for (Element e : document.select(search.getNextPage())) {
            String href = CrawlUtils.normalizeUrl(e.attr("href"), this.rule.getUrl());
            // 中文解码，针对69書吧
            urls.add(URLUtil.decode(href));
        }

        // 使用并行流处理分页 URL
        List<SearchResult> additionalResults = urls.parallelStream()
                .flatMap(url -> getSearchResults(url, null).stream())
                .toList();
        // 合并，不去重（去重用 union）
        List<SearchResult> unionAll = CollUtil.unionAll(firstPageResults, additionalResults);

        return SearchResultsHandler.handle(unionAll);
    }

    @SneakyThrows
    private List<SearchResult> getSearchResults(String url, Connection.Response resp) {
        Rule.Search rule = this.rule.getSearch();
        List<SearchResult> list = new ArrayList<>();
        // 搜索结果页 DOM
        Document document = resp == null ? jsoupConn(url, TIMEOUT_MILLS).get() : Jsoup.parse(resp.body());

        // 部分书源完全匹配会直接进入详情页，因此需要构造搜索结果
        if (document.select(rule.getResult()).isEmpty()) {
            String bookUrl = resp.url().toString();
            BookParser bookParser = new BookParser(config);
            Book book = bookParser.parse(bookUrl);
            if (StrUtil.isBlank(book.getBookName())) {
                return Collections.emptyList();
            }
            SearchResult build = SearchResult.builder()
                    .url(bookUrl)
                    .bookName(book.getBookName())
                    .author(book.getAuthor())
                    .latestChapter(book.getLatestChapter())
                    .latestUpdate(book.getLatestUpdate())
                    .build();
            list.add(build);
            return list;
        }

        Elements elements = document.select(rule.getResult());
        for (Element element : elements) {
            // jsoup 不支持一次性获取属性的值
            String href = element.select(rule.getBookName()).attr("href");
            String bookName = element.select(rule.getBookName()).text();
            // 以下为非必须属性，需判空，否则抛出 org.jsoup.helper.ValidationException: String must not be empty
            String author = StrUtil.isNotEmpty(rule.getAuthor())
                    ? element.select(rule.getAuthor()).text()
                    : null;
            String latestChapter = StrUtil.isNotEmpty(rule.getLatestChapter())
                    ? element.select(rule.getLatestChapter()).text()
                    : null;
            String update = StrUtil.isNotEmpty(rule.getUpdate())
                    ? element.select(rule.getUpdate()).text()
                    : null;

            // 针对书源 1：排除第一个 tr 表头
            if (bookName.isEmpty()) continue;

            SearchResult sr = SearchResult.builder()
                    .url(CrawlUtils.normalizeUrl(href, this.rule.getUrl()))
                    .bookName(bookName)
                    .author(author)
                    .latestChapter(latestChapter)
                    .latestUpdate(update)
                    .build();

            list.add(ChineseConverter.convert(sr, this.rule.getLanguage(), config.getLanguage()));
        }

        return list;
    }

    public static void printSearchResult(List<SearchResult> results) {
        ConsoleTable consoleTable = ConsoleTable.create().addHeader("序号", "书名", "作者", "最新章节", "最后更新时间");
        for (int i = 1; i <= results.size(); i++) {
            SearchResult r = results.get(i - 1);
            consoleTable.addBody(String.valueOf(i),
                    r.getBookName(),
                    r.getAuthor(),
                    r.getLatestChapter(),
                    r.getLatestUpdate());
        }
        Console.table(consoleTable);
    }

}