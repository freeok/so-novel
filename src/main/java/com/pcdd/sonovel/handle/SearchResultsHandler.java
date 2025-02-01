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
     * 优化源站的搜索结果
     */
    public List<SearchResult> handle(List<SearchResult> list) {
        // 按照作者分组
        Map<String, List<SearchResult>> authorGroups = list.stream()
                .collect(Collectors.groupingBy(SearchResult::getAuthor, LinkedHashMap::new, Collectors.toList()));

        // 对每个组进行排序
        return authorGroups.values().stream()
                // 根据每个作者对应的书籍数量进行降序排序
                .sorted((entry1, entry2) -> Integer.compare(entry2.size(), entry1.size()))
                // 每个作者的多个作品按书名排序，而只有一个作品的作者则不做排序
                .map(group -> group.size() > 1
                        ? group.stream().sorted(Comparator.comparing(SearchResult::getBookName)).toList()
                        : group)
                .flatMap(Collection::stream)
                .toList();
    }

}