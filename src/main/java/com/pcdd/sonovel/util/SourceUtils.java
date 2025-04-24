package com.pcdd.sonovel.util;

import cn.hutool.core.collection.CollUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author pcdd
 * Created at 2025/1/29
 */
@UtilityClass
public class SourceUtils {

    // 全部书源
    public final List<Integer> ALL_IDS = IntStream.rangeClosed(1, 18).boxed().toList();
    // 需要代理的书源
    public final List<Integer> PROXY_IDS = CollUtil.newArrayList(6, 7, 12, 16);
    // 支持聚合搜索的书源
    public final Collection<Integer> AGGREGATED_IDS = CollUtil.disjunction(ALL_IDS, PROXY_IDS);

    public int getCount() {
        return ALL_IDS.size();
    }

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