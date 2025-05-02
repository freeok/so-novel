package com.pcdd.sonovel.util;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.utils.JavetResourceUtils;
import com.caoccao.javet.values.reference.V8ValueFunction;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsCaller {

    // 每个线程绑定一个 V8Runtime（线程安全）
    private static final ThreadLocal<V8Runtime> THREAD_LOCAL_RUNTIME = ThreadLocal.withInitial(() -> {
        try {
            return V8Host.getV8Instance().createV8Runtime();
        } catch (JavetException e) {
            throw new RuntimeException("Failed to create V8 runtime", e);
        }
    });

    public static final String JS_TEMPLATE = """
            function func(r) {
                %s
                return r;
            }
            """;

    /**
     * 调用 jsCode 代码处理 input，并返回结果
     *
     * @param jsCode JS_TEMPLATE 代码 (动态逻辑)
     * @param input  输入参数 (如 HTML 字符串)
     * @return 处理后的结果
     */
    @SneakyThrows
    public String call(String jsCode, String input) {
        V8Runtime v8Runtime = THREAD_LOCAL_RUNTIME.get();
        // 先加载函数
        v8Runtime.getExecutor(JS_TEMPLATE.formatted(jsCode)).executeVoid();
        V8ValueFunction function = v8Runtime.getGlobalObject().get("func");
        return function.callString(null, input);
    }

    // 执行 JS_TEMPLATE 表达式，返回结果字符串
    @SneakyThrows
    public static String eval(String jsCode) {
        V8Runtime v8Runtime = THREAD_LOCAL_RUNTIME.get();
        return v8Runtime.getExecutor(jsCode).executeString();
    }

    // 调用 JS_TEMPLATE 函数
    @SneakyThrows
    public static Object callFunction(String jsFunctionCode, String functionName, Object... args) {
        V8Runtime v8Runtime = THREAD_LOCAL_RUNTIME.get();
        // 先加载函数
        v8Runtime.getExecutor(jsFunctionCode).executeVoid();
        V8ValueFunction function = v8Runtime.getGlobalObject().get(functionName);
        return function.callObject(null, args);
    }

    // 释放当前线程的 V8Runtime（可选，看线程池策略）
    public static void close() {
        V8Runtime v8Runtime = THREAD_LOCAL_RUNTIME.get();
        JavetResourceUtils.safeClose(v8Runtime);
        THREAD_LOCAL_RUNTIME.remove();
    }

}