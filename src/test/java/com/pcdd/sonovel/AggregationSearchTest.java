package com.pcdd.sonovel;

import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.handle.SearchResultsHandler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.SearchParser;
import com.pcdd.sonovel.util.ConfigUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试聚合搜索
 *
 * @author pcdd
 * Created at 2025/3/23
 */
class AggregationSearchTest {

    static {
        ConsoleLog.setLevel(Level.OFF);
    }

    /**
     * 聚合搜索必须选择查询对象
     * 1. 书名
     * 2. 作者
     */
    @SneakyThrows
    @Test
    void test01() {
        String kw = "玄鉴仙族";

        List<Integer> ids = List.of(1, 2, 3, 4, 5, 8, 11, 12, 14, 15, 16);
        ExecutorService threadPool = Executors.newFixedThreadPool(ids.size());
        List<List<SearchResult>> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(ids.size());
        Console.log("书源搜索速度排名如下");

        for (Integer id : ids) {
            threadPool.execute(() -> {
                AppConfig config = ConfigUtils.config();
                config.setSourceId(id);
                Source source = new Source(config);
                Rule rule = source.rule;
                List<SearchResult> res = new SearchParser(config).parse(kw);
                Console.log("ID: {} ({} {}) 搜索结果数: {}", id, rule.getName(), rule.getUrl(), res.size());
                results.add(res);
                latch.countDown();
            });
        }
        latch.await();

        List<SearchResult> flatList = new ArrayList<>();
        results.forEach(flatList::addAll);
        List<SearchResult> aggregateSearchResult = SearchResultsHandler.aggregateSort(flatList, kw);
        SearchParser.printAggregateSearchResult(aggregateSearchResult);
        testTextSimilarity(aggregateSearchResult, kw);
    }

    void testTextSimilarity(List<SearchResult> result, String kw) {
        ConsoleTable table = ConsoleTable.create();
        table.addHeader("书名", "相似度");
        result.forEach(o -> {
            double similar = StrUtil.similar(kw, o.getBookName());
            table.addBody(o.getBookName(), String.valueOf(similar));
        });
        System.out.println(table);
    }

}