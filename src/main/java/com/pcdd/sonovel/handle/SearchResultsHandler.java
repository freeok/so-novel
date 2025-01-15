package com.pcdd.sonovel.handle;

import com.pcdd.sonovel.model.SearchResult;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pcdd
 * Created at 2025/1/15
 */
@UtilityClass
public class SearchResultsHandler {

    /**
     * 对源站的搜索结果进行优化
     */
    public List<SearchResult> handle(List<SearchResult> list) {
        // 按照作者分组
        Map<String, List<SearchResult>> authorGroups = list.stream()
                .collect(Collectors.groupingBy(SearchResult::getAuthor, LinkedHashMap::new, Collectors.toList()));

        // 对每个组进行排序
        return authorGroups.values().stream()
                .map(group -> group.size() > 1
                        ? group.stream().sorted(Comparator.comparing(SearchResult::getBookName)).toList()
                        : group)
                .flatMap(Collection::stream)
                .toList();
    }

}