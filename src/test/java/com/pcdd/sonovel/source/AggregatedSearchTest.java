package com.pcdd.sonovel.source;

import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.pcdd.sonovel.action.AggregatedSearchAction;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parse.SearchParser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 测试聚合搜索
 *
 * @author pcdd
 * Created at 2025/3/23
 */
class AggregatedSearchTest {

    static {
        ConsoleLog.setLevel(Level.OFF);
    }

    @SneakyThrows
    @Test
    void test01() {
        String kw = "玄鉴仙族";
        List<SearchResult> results = AggregatedSearchAction.getSearchResults(kw);
        SearchParser.printAggregateSearchResult(results);
        testTextSimilarity(results, kw);
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