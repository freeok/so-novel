package com.pcdd.sonovel.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
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

    public void downloadFromUrl() {
        Console.print(render("==> 请输入书籍详情页网址: ", "green"));
        String bookUrl = sc.nextLine().strip();
        Rule rule = new Source(config).rule;
        // www.69shuba.me 的链接转换为 69shuba.cx 的
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
        // tocParser.shutdown();
        Console.log("<== 《{}》({})，共计 {} 章", sr.getBookName(), sr.getAuthor(), toc.size());
        double res = new Crawler(config).crawl(sr, toc);
        Console.log("<== 完成！总耗时 {} s", NumberUtil.round(res, 2));
    }

    public void downloadByKeyword() {
        // 1. 查询
        Console.print(render("==> 请输入书名或作者（宁少字别错字）: ", "green"));
        String keyword = sc.nextLine().strip();
        if (keyword.isEmpty()) return;
        List<SearchResult> searchResults = new Crawler(config).search(keyword);
        if (CollUtil.isEmpty(searchResults)) {
            return;
        }

        // 2. 打印搜索结果
        new SearchParser(config).printSearchResult(searchResults);

        // 3. 下载
        new DownloadAction(config).execute(searchResults);
    }

    @SneakyThrows
    public void execute() {
        Source source = new Source(config.getSourceId());

        // URL 下载的小说，rule.json 删除 search
        if (source.rule.getSearch() == null) {
            downloadFromUrl();
        } else {
            downloadByKeyword();
        }
    }

}