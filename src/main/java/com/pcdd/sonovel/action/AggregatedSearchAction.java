package com.pcdd.sonovel.action;

import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.handle.SearchResultsHandler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.SearchParser;
import com.pcdd.sonovel.util.ConfigUtils;
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

    private final List<Integer> ids = List.of(1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 14, 15, 16);

    @SneakyThrows
    public void execute() {
        Scanner sc = Console.scanner();
        Console.print(render("==> 请输入书名或作者（尽量输完整）: ", "green"));
        String kw = sc.nextLine().strip();
        ExecutorService threadPool = Executors.newFixedThreadPool(ids.size());
        List<List<SearchResult>> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(ids.size());

        for (Integer id : ids) {
            threadPool.execute(() -> {
                AppConfig conf = ConfigUtils.config();
                conf.setSourceId(id);
                Source source = new Source(conf);
                Rule rule = source.rule;
                List<SearchResult> res = new SearchParser(conf).parse(kw);
                Console.log("书源 {} ({}) 搜索结果数: {}", id, rule.getName(), res.size());
                results.add(res);
                latch.countDown();
            });
        }
        latch.await();

        List<SearchResult> flatList = new ArrayList<>();
        results.forEach(flatList::addAll);
        List<SearchResult> searchResults = SearchResultsHandler.aggregateSort(flatList, kw);

        SearchParser.printAggregateSearchResult(searchResults);

        new DownloadAction(null).execute(searchResults);
    }

}