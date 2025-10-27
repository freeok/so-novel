package com.pcdd.sonovel.handle;

import cn.hutool.core.util.StrUtil;
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
     * 优化源站的搜索结果（排序）
     */
    public List<SearchResult> sort(List<SearchResult> list) {
        // 先按作者分组
        Map<String, List<SearchResult>> authorGroups = list.stream()
                .collect(Collectors.groupingBy(SearchResult::getAuthor, LinkedHashMap::new, Collectors.toList()));

        // 再对每个组进行排序
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

    /**
     * 优化源站搜索结果（排序、过滤）
     */
    public List<SearchResult> filterSort(List<SearchResult> list, String kw) {
        double bookNameScore = getSimilarity(list, kw, "bookName");
        double authorScore = getSimilarity(list, kw, "author");
        boolean isAuthorSearch = bookNameScore < authorScore;

        // 缓存相似度
        Map<SearchResult, Double> similarityMap = new HashMap<>();
        for (SearchResult sr : list) {
            double score = StrUtil.similar(kw, isAuthorSearch ? sr.getAuthor() : sr.getBookName());
            similarityMap.put(sr, score);
        }

        // 排序器统一封装，复用
        Comparator<SearchResult> comparator = (o1, o2) -> {
            double score1 = similarityMap.get(o1);
            double score2 = similarityMap.get(o2);
            if (score1 != score2) {
                return Double.compare(score2, score1); // 按相似度降序
            }
            return isAuthorSearch
                    ? o1.getBookName().compareTo(o2.getBookName())
                    : o1.getAuthor().compareTo(o2.getAuthor());
        };

        // 过滤低相似度搜索结果
        List<SearchResult> filtered = list.stream()
                .filter(sr -> similarityMap.get(sr) > 0.3)
                .sorted(comparator)
                .toList();

        // 若过滤后为空，则返回仅排序的搜索结果
        return filtered.isEmpty()
                ? list.stream().filter(sr -> similarityMap.get(sr) > 0).sorted(comparator).toList()
                : filtered;
    }


    /**
     * 计算权重，用于判断关键字是书名还是作者
     */
    private double getSimilarity(List<SearchResult> data, String kw, String type) {
        boolean isShortQuery = kw.length() <= 4; // 关键词很短，可能是作者
        boolean isLongQuery = kw.length() >= 10; // 关键词很长，可能是书名

        return data.stream().mapToDouble(sr -> {
            double similar = StrUtil.similar(kw, "bookName".equals(type) ? sr.getBookName() : sr.getAuthor());

            // 短关键词匹配更严格
            if (isShortQuery) {
                if (similar == 1.0) return 12; // 强调完全匹配
                if (similar >= 0.8) return Math.pow(similar, 3) * 8;
                if (similar >= 0.7) return similar * 5;
                return 0; // 低匹配直接归零，避免误判
            }

            // 长关键词匹配更宽松
            if (isLongQuery) {
                if (similar == 1.0) return 10;
                if (similar >= 0.85) return Math.pow(similar, 3) * 8;
                if (similar >= 0.7) return Math.pow(similar, 2) * 5;
                if (similar >= 0.5) return similar * 3;
                return similar * 1.2;
            }

            // 普通匹配规则
            if (similar == 1.0) return 10;
            if (similar >= 0.85) return Math.pow(similar, 3) * 8;
            if (similar >= 0.7) return Math.pow(similar, 2) * 5;
            if (similar >= 0.5) return similar * 3;
            return 0; // 默认情况，低匹配归零
        }).sum();
    }

}