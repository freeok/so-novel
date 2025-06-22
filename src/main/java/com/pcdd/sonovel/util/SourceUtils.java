package com.pcdd.sonovel.util;

import cn.hutool.core.collection.CollUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;

/**
 * @author pcdd
 * Created at 2025/1/29
 */
@UtilityClass
public class SourceUtils {

    // 全部书源
    public final List<Integer> ALL_IDS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21);
    // 不支持搜索的、搜索有限流的、搜索意义不大的、暂时无法访问的书源
    public final List<Integer> SKIP_IDS = CollUtil.newArrayList(6, 7, 12, 16);
    // 纳入聚合搜索的书源
    public final Collection<Integer> AGGREGATED_IDS = CollUtil.disjunction(ALL_IDS, SKIP_IDS);

    public List<Source> getSearchableSources() {
        return AGGREGATED_IDS.stream()
                .map(id -> {
                    AppConfig config = ConfigUtils.config();
                    config.setSourceId(id);
                    return new Source(config);
                })
                .toList();
    }

}