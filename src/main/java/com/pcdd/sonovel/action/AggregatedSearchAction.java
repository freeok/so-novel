package com.pcdd.sonovel.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.handle.SearchResultsHandler;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.SearchParser;
import com.pcdd.sonovel.util.SourceUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * 聚合搜索，从全部书源搜索
 *
 * @author pcdd
 * Created at 2025/3/26
 */
@AllArgsConstructor
public class AggregatedSearchAction {

    public void execute() {
        Scanner sc = Console.scanner();
        Console.print(render("==> 请输入书名或作者（尽量输完整）: ", "green"));
        String kw = sc.nextLine().strip();
        if (kw.isEmpty()) return;

        List<SearchResult> results = getSearchResults(kw);

        if (CollUtil.isEmpty(results)) {
            Console.log(render("聚合搜索结果为空！", "yellow"));
            return;
        }

        SearchParser.printAggregateSearchResult(results);

        new DownloadAction(null).execute(results);
    }

    @SneakyThrows
    public static List<SearchResult> getSearchResults(String kw) {
        List<List<SearchResult>> results = new ArrayList<>();
        List<Source> searchableSources = SourceUtils.getSearchableSources();
        ExecutorService threadPool = Executors.newFixedThreadPool(searchableSources.size());
        CountDownLatch latch = new CountDownLatch(searchableSources.size());

        for (Source source : searchableSources) {
            threadPool.execute(() -> {
                List<SearchResult> res = new SearchParser(source.config).parse(kw);

                if (CollUtil.isNotEmpty(res)) {
                    Rule rule = source.rule;
                    Console.log("<== 书源 {} ({})\t搜索到 {} 条记录", rule.getId(), rule.getName(), res.size());
                    results.add(res);
                }

                latch.countDown();
            });
        }

        latch.await();
        List<SearchResult> flatList = new ArrayList<>();
        results.forEach(flatList::addAll);
        return SearchResultsHandler.aggregateSort(flatList, kw);
    }

}