package com.pcdd.sonovel.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
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
    private final Scanner sc = Console.scanner();
    private static final String GREEN = "green";

    public void downloadFromUrl(String url) {
        Rule rule = new Source(config).rule;
        url = JsoupUtils.invokeJs(rule.getBook().getUrl(), url);
        Book book = new BookParser(config).parse(url);
        SearchResult sr = SearchResult.builder()
                .sourceId(config.getSourceId())
                .url(url)
                .bookName(book.getBookName())
                .author(book.getAuthor())
                .latestChapter(book.getLatestChapter())
                .lastUpdateTime(book.getLastUpdateTime())
                .build();
        new DownloadAction().execute(ListUtil.toList(sr));
    }

    public void downloadByKeyword(String keyword) {
        if (StrUtil.isEmpty(keyword)) return;
        List<SearchResult> searchResults = search(keyword);
        if (CollUtil.isEmpty(searchResults)) {
            return;
        }
        new SearchParser(config).printSearchResult(searchResults);
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
            Console.print(render("==> 请输入书籍详情页网址: ", GREEN));
            downloadFromUrl(sc.nextLine().strip());
        } else {
            Console.print(render("==> 请输入书名或作者（宁少字别错字）: ", GREEN));
            downloadByKeyword(sc.nextLine().strip());
        }
    }

}