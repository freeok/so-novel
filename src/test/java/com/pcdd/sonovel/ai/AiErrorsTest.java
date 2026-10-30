package com.pcdd.sonovel.ai;

import org.junit.jupiter.api.Test;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI 错误翻译的测试：校对失败时命令行用户必须看到「下一步做什么」，
 * 而不是网关吐出的原始 JSON。
 */
class AiErrorsTest {

    /**
     * 国际版未登录的典型错误（EAC 网关原样回传 Qoder 的 pricing 提示）
     */
    @Test
    void intlNotLoggedInPointsToLogin() {
        String body = "{\"error\":{\"message\":\"qoder: {\\\"pricingUrl\\\":\\\"https://qoder.com/pricing\\\"} (112/model_error)\",\"type\":\"server_error\",\"code\":\"SERVER\"}}";
        String msg = AiErrors.describe(502, body, "http://127.0.0.1:18326/v1/chat/completions");

        assertTrue(msg.contains("未登录") || msg.contains("eac-qwen-cn"), msg);
        assertTrue(msg.contains("HTTP 502"), msg);
        // 原文必须保留，便于排查
        assertTrue(msg.contains("pricingUrl"), "原文不能丢：" + msg);
    }

    /**
     * 无健康上游同样要提示改用国内版
     */
    @Test
    void noHealthyUpstreamIsActionable() {
        String msg = AiErrors.describe(503, "qoder: no healthy upstream", "http://x/v1");
        assertTrue(msg.contains("eac-qwen-cn") || msg.contains("未登录"), msg);
    }

    /**
     * 限流要提示稍后重试或换模型
     */
    @Test
    void throttledSuggestsRetry() {
        String msg = AiErrors.describe(502,
                "Qoder (中国版)：模型 qfmodel 的可用账号均在限流中，最近一个将于 2026-10-30 00:00:00 解禁",
                "http://x/v1");
        assertTrue(msg.contains("限流") || msg.contains("稍后"), msg);
    }

    /**
     * 连不上本地网关：必须点名「确认 DSH 的 Our Free Model 插件已启动」
     */
    @Test
    void connectionRefusedNamesTheGateway() {
        String msg = AiErrors.describe(new ConnectException("Failed to connect to /127.0.0.1:18326"),
                "http://127.0.0.1:18326/v1");
        assertTrue(msg.contains("Our Free Model") || msg.contains("网关"), msg);
        assertTrue(msg.contains("18326"), msg);
    }

    /**
     * 401 鉴权失败
     */
    @Test
    void unauthorizedIsKeyProblem() {
        String msg = AiErrors.describe(401, "{\"error\":{\"message\":\"Invalid API key\"}}", "http://x/v1");
        assertTrue(msg.contains("密钥"), msg);
    }

    /**
     * 非 JSON 的 HTML 错误页不能把解析器搞崩，原样兜底
     */
    @Test
    void nonJsonBodyDoesNotThrow() {
        String html = "<html><body>502 Bad Gateway</body></html>";
        String msg = assertDoesNotThrow(() -> AiErrors.describe(502, html, "http://x/v1"));
        assertTrue(msg.contains("HTTP 502"), msg);
    }

    /**
     * 空 body / 空消息也不崩
     */
    @Test
    void blankInputsTolerated() {
        assertDoesNotThrow(() -> AiErrors.describe(500, "", "http://x/v1"));
        assertDoesNotThrow(() -> AiErrors.describe((Throwable) null, "http://x/v1"));
    }

}
