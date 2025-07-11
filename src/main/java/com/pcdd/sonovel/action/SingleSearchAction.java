package com.pcdd.sonovel.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.*;
import com.pcdd.sonovel.parse.BookParser;
import com.pcdd.sonovel.parse.SearchParser;
import com.pcdd.sonovel.parse.TocParser;
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

    public void downloadFromUrl(int sourceId) {
        config.setSourceId(sourceId);
        Console.print(render("==> 请输入书籍详情页网址: ", GREEN));
        String bookUrl = sc.nextLine().strip();
        Rule rule = new Source(config).rule;
        bookUrl = JsoupUtils.invokeJs(rule.getBook().getUrl(), bookUrl);
        Book book = new BookParser(config).parse(bookUrl);
        SearchResult sr = SearchResult.builder()
                .url(book.getUrl())
                .bookName(book.getBookName())
                .author(book.getAuthor())
                .latestChapter(book.getLatestChapter())
                .lastUpdateTime(book.getLastUpdateTime())
                .build();

        TocParser tocParser = new TocParser(config);
        List<Chapter> toc = tocParser.parse(sr.getUrl());
        Console.log("<== 《{}》({})，共计 {} 章", sr.getBookName(), sr.getAuthor(), toc.size());
        // 重复请求详情页
        double res = new Crawler(config).crawl(sr.getUrl(), toc);
        Console.log(render("<== 完成！总耗时 {} s", GREEN), NumberUtil.round(res, 2));
    }

    public void downloadByKeyword(int sourceId) {
        config.setSourceId(sourceId);
        // 1. 查询
        Console.print(render("==> 请输入书名或作者（宁少字别错字）: ", GREEN));
        String kw = sc.nextLine().strip();
        if (StrUtil.isEmpty(kw)) return;
        List<SearchResult> searchResults = new Crawler(config).search(kw);
        if (CollUtil.isEmpty(searchResults)) {
            return;
        }

        // 2. 打印搜索结果
        new SearchParser(config).printSearchResult(searchResults);

        // 3. 下载
        new DownloadAction().execute(searchResults);
    }

    @SneakyThrows
    public void execute() {
        Console.print(render("==> 请输入书源 ID: ", GREEN));
        int sourceId = Integer.parseInt(sc.nextLine());
        Source source = new Source(sourceId);

        if (source.rule.getSearch() == null) {
            downloadFromUrl(sourceId);
        } else {
            downloadByKeyword(sourceId);
        }
    }

}