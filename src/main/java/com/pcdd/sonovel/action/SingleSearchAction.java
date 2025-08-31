package com.pcdd.sonovel.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.parse.SearchParser;
import com.pcdd.sonovel.parse.SearchParserQuanben5;
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Scanner;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * 搜索指定书源
 *
 * @author pcdd
 * Created at 2024/11/10
 */
@AllArgsConstructor
public class SingleSearchAction {

    private final AppConfig config;
    public final Scanner sc = Console.scanner();
    public static final String GREEN = "green";

    public void downloadFromUrl(AppConfig config) {
        Rule rule = new Source(config).rule;
        Console.print(render("==> 请输入书籍详情页网址: ", GREEN));
        String bookUrl = sc.nextLine().strip();
        bookUrl = JsoupUtils.invokeJs(rule.getBook().getUrl(), bookUrl);
        Book book = new BookParser(config).parse(bookUrl);
        SearchResult sr = SearchResult.builder()
                .url(bookUrl)
                .bookName(book.getBookName())
                .author(book.getAuthor())
                .latestChapter(book.getLatestChapter())
                .lastUpdateTime(book.getLastUpdateTime())
                .build();
        Console.log("<== 《{}》({})，正在解析目录...", sr.getBookName(), sr.getAuthor());
        // 重复请求详情页
        new Crawler(config).crawl(sr.getUrl());
    }

    public void downloadByKeyword(AppConfig config) {
        // 1. 查询
        Console.print(render("==> 请输入书名或作者（宁少字别错字）: ", GREEN));
        String kw = sc.nextLine().strip();
        if (StrUtil.isEmpty(kw)) return;
        List<SearchResult> searchResults = search(kw);
        if (CollUtil.isEmpty(searchResults)) {
            return;
        }

        // 2. 打印搜索结果
        new SearchParser(config).printSearchResult(searchResults);

        // 3. 下载
        new DownloadAction().execute(searchResults);
    }

    public List<SearchResult> search(String keyword) {
        Console.log("<== 正在搜索...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<SearchResult> searchResults = "proxy-rules.json".equals(config.getActiveRules()) && config.getSourceId() == 2
                ? new SearchParserQuanben5(config).parse(keyword)
                : new SearchParser(config).parse(keyword, true);

        stopWatch.stop();
        Console.log("<== 搜索到 {} 条记录，耗时 {} s", searchResults.size(), NumberUtil.round(stopWatch.getTotalTimeSeconds(), 2));
        return searchResults;
    }

    @SneakyThrows
    public void execute() {
        if (config.getSourceId() == -1) {
            Console.print(render("==> 请指定书源 ID: ", GREEN));
            config.setSourceId(Integer.parseInt(sc.nextLine()));
        }

        Source source = new Source(config);
        Rule r = source.rule;

        if (r.getSearch() == null || r.getSearch().isDisabled()) {
            downloadFromUrl(config);
        } else {
            downloadByKeyword(config);
        }
    }

}