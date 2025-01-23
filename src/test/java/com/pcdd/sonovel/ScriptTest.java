package com.pcdd.sonovel;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.script.ScriptUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class ScriptTest {

    @Test
    @SneakyThrows
    void rule6() {
        String script = ResourceUtil.readUtf8Str("js/rule-6.js");
        Object invoke = ScriptUtil.invoke(script, "getParamB", "吞噬星空");
        System.out.println(invoke);
    }

}