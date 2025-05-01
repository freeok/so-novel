package com.pcdd.sonovel.util;

import lombok.experimental.UtilityClass;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

@UtilityClass
public class JsCaller {

    /**
     * 调用 JS 代码处理 rValue，并返回结果
     *
     * @param jsCode JS 代码 (动态逻辑)
     * @param rValue 输入参数 (如 HTML 字符串)
     * @return 处理后的结果
     */
    public String call(String jsCode, String rValue) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .build()) {
            context.eval("js", """
                    globalThis.__callJs = function(jsCode, r) {
                        let func = new Function('r', jsCode + '; return r;');
                        return func(r);
                    };
                    """);

            Value callJsFunc = context.getBindings("js").getMember("__callJs");
            Value result = callJsFunc.execute(jsCode, rValue);
            return result.asString();
        }
    }

}