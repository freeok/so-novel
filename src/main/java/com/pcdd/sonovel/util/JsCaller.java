package com.pcdd.sonovel.util;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import com.caoccao.javet.values.reference.V8ValueFunction;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsCaller {

    private static final JavetEnginePool<NodeRuntime> POOL = new JavetEnginePool<>();
    private static final String JS_TEMPLATE = """
            function func(r) {
                %s;
                return r;
            }
            """;

    static {
        // Node.js Mode
        POOL.getConfig().setJSRuntimeType(JSRuntimeType.Node);
    }

    /**
     * 调用 jsCode 传入后的 func 函数
     *
     * @param jsCode JS_TEMPLATE 代码 (动态逻辑)
     * @param input  输入参数 (如 HTML 字符串)
     * @return 执行结果
     */
    @SneakyThrows
    public String call(String jsCode, String input) {
        try (IJavetEngine<NodeRuntime> engine = POOL.getEngine()) {
            // Console.log("V8引擎池状态 => 正在使用={}, 空闲={}, 已释放={}", POOL.getActiveEngineCount(), POOL.getIdleEngineCount(), POOL.getReleasedEngineCount());
            NodeRuntime nodeRuntime = engine.getV8Runtime();
            String scriptString = JS_TEMPLATE.formatted(jsCode);
            nodeRuntime.getExecutor(scriptString).executeVoid();

            try (V8ValueFunction function = nodeRuntime.getGlobalObject().get("func")) {
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
        try (IJavetEngine<NodeRuntime> engine = POOL.getEngine()) {
            NodeRuntime node = engine.getV8Runtime();
            // 先加载函数
            node.getExecutor(jsFunctionCode).executeVoid();
            V8ValueFunction function = node.getGlobalObject().get(functionName);
            return function.callObject(null, args);
        }
    }

    /**
     * 执行 JS 表达式，返回结果字符串
     */
    @SneakyThrows
    public String eval(String jsCode) {
        try (IJavetEngine<NodeRuntime> engine = POOL.getEngine()) {
            NodeRuntime node = engine.getV8Runtime();
            return node.getExecutor(jsCode).executeString();
        }
    }

}