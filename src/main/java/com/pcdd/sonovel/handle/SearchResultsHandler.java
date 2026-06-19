package com.pcdd.sonovel.handle;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.SearchResult;
import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * @author pcdd
 * Created at 2025/1/15
 */
@UtilityClass
public class SearchResultsHandler {

    /**
     * 优化源站搜索结果（过滤低相似度结果、按相似度降序）
     */
    public List<SearchResult> filterAndSort(List<SearchResult> list, String kw) {
        if (CollUtil.isEmpty(list)) return list;

        // 预计算书名和作者相似度
        Map<SearchResult, Double> bookSim = new HashMap<>();
        Map<SearchResult, Double> authorSim = new HashMap<>();
        for (SearchResult sr : list) {
            bookSim.put(sr, StrUtil.similar(kw, sr.getBookName()));
            authorSim.put(sr, StrUtil.similar(kw, sr.getAuthor()));
        }

        boolean isAuthorSearch = computeWeight(bookSim, kw) < computeWeight(authorSim, kw);

        // 根据匹配类型选取最终相似度
        Map<SearchResult, Double> similarityMap = new HashMap<>();
        for (SearchResult sr : list) {
            similarityMap.put(sr, isAuthorSearch ? authorSim.get(sr) : bookSim.get(sr));
        }

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

        List<SearchResult> filtered = Collections.emptyList();
        if (AppConfigLoader.APP_CONFIG.getSearchFilter() == 1) {
            filtered = list.stream()
                    .filter(sr -> similarityMap.get(sr) > 0.3) // 过滤低相似度搜索结果
                    .sorted(comparator)
                    .toList();
        }

        // 若过滤后为空，则返回仅排序的搜索结果
        return filtered.isEmpty()
                ? list.stream().filter(sr -> similarityMap.get(sr) > 0).sorted(comparator).toList()
                : filtered;
    }

    /**
     * 计算权重，用于判断关键字是书名还是作者
     */
    private double computeWeight(Map<SearchResult, Double> simMap, String kw) {
        boolean isShortQuery = kw.length() <= 4;
        boolean isLongQuery = kw.length() >= 10;

        return simMap.values()
                .stream()
                .mapToDouble(s -> weight(s, isShortQuery, isLongQuery))
                .sum();
    }

    private double weight(double s, boolean isShort, boolean isLong) {
        if (isShort) {
            if (s == 1.0) return 12;
            if (s >= 0.8) return s * s * s * 8;
            if (s >= 0.7) return s * 5;
            return 0;
        }
        if (isLong) {
            if (s == 1.0) return 10;
            if (s >= 0.85) return s * s * s * 8;
            if (s >= 0.7) return s * s * 5;
            if (s >= 0.5) return s * 3;
            return s * 1.2;
        }
        if (s == 1.0) return 10;
        if (s >= 0.85) return s * s * s * 8;
        if (s >= 0.7) return s * s * 5;
        if (s >= 0.5) return s * 3;
        return 0;
    }

}