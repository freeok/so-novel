package com.pcdd.sonovel.utils;

import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import com.caoccao.javet.values.reference.V8ValueFunction;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsCaller {

    private static final JavetEnginePool<V8Runtime> POOL = new JavetEnginePool<>();
    private static final String JS_TEMPLATE = """
            function func(r) {
                %s;
                return r;
            }
            """;

    /**
     * 调用 jsCode 传入后的 func 函数
     *
     * @param jsCode JS_TEMPLATE 代码 (动态逻辑)
     * @param input  输入参数 (如 HTML 字符串)
     * @return 执行结果
     */
    @SneakyThrows
    public String call(String jsCode, String input) {
        try (IJavetEngine<V8Runtime> engine = POOL.getEngine()) {
            // Console.log("V8引擎池状态 => 正在使用={}, 空闲={}, 已释放={}", POOL.getActiveEngineCount(), POOL.getIdleEngineCount(), POOL.getReleasedEngineCount());
            V8Runtime v8Runtime = engine.getV8Runtime();
            String scriptString = JS_TEMPLATE.formatted(jsCode);
            // Console.log("scriptString = " + scriptString);
            v8Runtime.getExecutor(scriptString).executeVoid();

            try (V8ValueFunction function = v8Runtime.getGlobalObject().get("func")) {
                return function.callString(null, input);
            }
        }
    }

    /**
     * 调用 JS 函数
     *
     * @param jsFunctionCode JS 函数代码
     * @param functionName   函数名
     * @param args           参数
     * @return 执行结果
     */
    @SneakyThrows
    public Object callFunction(String jsFunctionCode, String functionName, Object... args) {
        try (IJavetEngine<V8Runtime> engine = POOL.getEngine()) {
            V8Runtime v8Runtime = engine.getV8Runtime();
            // 先加载函数
            v8Runtime.getExecutor(jsFunctionCode).executeVoid();
            V8ValueFunction function = v8Runtime.getGlobalObject().get(functionName);
            return function.callObject(null, args);
        }
    }

    /**
     * 执行 JS 表达式，返回结果字符串
     */
    @SneakyThrows
    public String eval(String jsCode) {
        try (IJavetEngine<V8Runtime> engine = POOL.getEngine()) {
            V8Runtime v8Runtime = engine.getV8Runtime();
            return v8Runtime.getExecutor(jsCode).executeString();
        }
    }

}