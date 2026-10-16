package com.pcdd.sonovel.actions;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.handler.SearchResultsHandler;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.model.SourceSearchStatus;
import com.pcdd.sonovel.parser.SearchParser;
import com.pcdd.sonovel.utils.SourceUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
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

        new DownloadAction().execute(results);
    }

    /**
     * 聚合搜索，返回排序后的结果以及每个书源的连接/搜索状态
     */
    @SneakyThrows
    public static AggregatedOutcome getSearchOutcome(String kw) {
        Console.log("<== 搜索关键字 “{}”", kw);
        List<SearchResult> results = Collections.synchronizedList(new ArrayList<>());
        List<SourceSearchStatus> statuses = Collections.synchronizedList(new ArrayList<>());
        List<Source> searchableSources = SourceUtils.getSearchableSources();
        CountDownLatch latch = new CountDownLatch(searchableSources.size());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Source source : searchableSources) {
                executor.execute(() -> {
                    SearchParser parser = new SearchParser(source.config);
                    List<SearchResult> res = Collections.emptyList();
                    try {
                        res = parser.parse(kw);
                        if (CollUtil.isNotEmpty(res)) {
                            Rule rule = source.rule;
                            Console.log("<== 书源 {} ({})\t搜索到 {} 条记录", rule.getId(), rule.getName(), res.size());
                            results.addAll(res);
                        }
                    } catch (Exception e) {
                        // parser 内部已捕获绝大多数异常，此处兜底
                        parser.searchStatus = "error";
                        parser.statusMessage = e.getMessage() == null ? e.toString() : e.getMessage();
                        Console.error("搜索源 {} 异常：{}", source.rule.getName(), parser.statusMessage);
                    } finally {
                        Rule rule = source.rule;
                        statuses.add(SourceSearchStatus.builder()
                                .id(rule.getId())
                                .name(rule.getName())
                                .url(rule.getUrl())
                                .status(parser.searchStatus)
                                .count(res.size())
                                .elapsedMs(parser.elapsedMs)
                                .message(parser.statusMessage)
                                .build());
                        latch.countDown();
                    }
                });
            }

            latch.await();
        }

        List<SearchResult> sorted = SearchResultsHandler.filterAndSort(results, kw);
        statuses.sort(Comparator.comparingInt(SourceSearchStatus::getId));
        return new AggregatedOutcome(sorted, statuses);
    }

    /**
     * 仅获取聚合搜索结果（保留给 TUI / 旧调用方）
     */
    public static List<SearchResult> getSearchResults(String kw) {
        return getSearchOutcome(kw).getResults();
    }

    /**
     * 聚合搜索产物：排序后的结果 + 逐源状态
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class AggregatedOutcome {
        private List<SearchResult> results;
        private List<SourceSearchStatus> sourceStatus;
    }

}