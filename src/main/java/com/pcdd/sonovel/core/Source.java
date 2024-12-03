package com.pcdd.sonovel.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.Rule;

import static org.jline.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class Source {

    public final Rule rule;

    public Source(int id) {
        String jsonStr = null;

        try {
            // 根据 sourceId 获取对应书源规则
            jsonStr = ResourceUtil.readUtf8Str("rule/rule" + id + ".json");
        } catch (Exception e) {
            Console.error(render("@|red 书源规则初始化失败，请检查配置项 source-id|@"));
            Console.error(render("@|red 错误信息：{}|@"), e.getMessage());
            System.exit(1);
        }

        // json 封装进 Rule
        this.rule = JSONUtil.toBean(jsonStr, Rule.class);
    }

}
