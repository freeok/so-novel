package com.pcdd.sonovel.handler;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 回归测试：搜索结果过滤排序对 null 作者 / null 书名必须安全。
 * <p>
 * 背景：bqg.info 搜索结果不返回作者（author=null），{@link StrUtil#similar(CharSequence, CharSequence)}
 * 对 null 抛 NPE，曾沿 AggregatedSearchServlet 逃逸为 HTTP 500（Jetty HTML 错误页）。
 */
class SearchResultsHandlerTest {

    static SearchResult result(String bookName, String author, String url) {
        return SearchResult.builder().bookName(bookName).author(author).url(url).build();
    }

    /**
     * 探针反例证据（RED 机制）：根因确实存在——hutool 相似度对 null 字符串抛 NPE。
     * 证明：若 filterAndSort 不做 null 安全包裹，下面的用例必然变红。
     */
    @Test
    void similar_throwsOnNull_demonstratesRootCause() {
        assertThrows(NullPointerException.class, () -> StrUtil.similar("斗罗大陆", null));
    }

    /**
     * 探针：author=null 时过滤排序不得抛异常，且书名命中的书应被保留。
     */
    @Test
    void filterAndSort_nullAuthor_doesNotThrowAndKeepsBook() {
        List<SearchResult> in = new ArrayList<>();
        in.add(result("斗罗大陆", null, "https://example.com/1"));

        List<SearchResult> out = assertDoesNotThrow(
                () -> SearchResultsHandler.filterAndSort(in, "斗罗大陆"));
        assertEquals(1, out.size());
        assertEquals("斗罗大陆", out.get(0).getBookName());
    }

    /**
     * bookName=null（作者命中）时同样不得抛异常。
     */
    @Test
    void filterAndSort_nullBookName_doesNotThrow() {
        List<SearchResult> in = new ArrayList<>();
        in.add(result(null, "唐家三少", "https://example.com/2"));

        assertDoesNotThrow(() -> SearchResultsHandler.filterAndSort(in, "唐家三少"));
    }

    /**
     * 精确匹配的书应排在不相关书之前（不相关书相似度为 0 会被过滤）。
     */
    @Test
    void filterAndSort_exactMatchRanksFirst() {
        List<SearchResult> in = List.of(
                result("一个毫不相干的长篇小说名字xyz", "路人甲", "https://example.com/b"),
                result("斗罗大陆", "唐家三少", "https://example.com/a"));

        List<SearchResult> out = SearchResultsHandler.filterAndSort(in, "斗罗大陆");
        assertFalse(out.isEmpty());
        assertEquals("斗罗大陆", out.get(0).getBookName());
    }

    /**
     * 空列表安全返回空，不抛异常。
     */
    @Test
    void filterAndSort_emptyList_returnsEmpty() {
        List<SearchResult> out = assertDoesNotThrow(
                () -> SearchResultsHandler.filterAndSort(new ArrayList<>(), "斗罗大陆"));
        assertTrue(out.isEmpty());
    }

}
