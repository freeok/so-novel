package com.pcdd.sonovel.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.Rule;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class Source {

    public final Rule rule;

    public Source(int sourceId) {
        // 根据 sourceId 获取对应书源规则
        String jsonStr = ResourceUtil.readUtf8Str("rule/rule" + sourceId + ".json");
        // json 封装进 Rule
        this.rule = JSONUtil.toBean(jsonStr, Rule.class);
    }

}
