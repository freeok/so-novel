package com.pcdd.sonovel.parse;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.Rule;

/**
 * @author pcdd
 */
public abstract class Parser {

    public final Rule rule;

    protected Parser(int sourceId) {
        // 根据 ruleId 获取对应 json 文件内容
        String jsonStr = ResourceUtil.readUtf8Str("rule/rule" + sourceId + ".json");
        // json 封装进 Rule
        this.rule = JSONUtil.toBean(jsonStr, Rule.class);
    }

}
