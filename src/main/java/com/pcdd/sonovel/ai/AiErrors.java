package com.pcdd.sonovel.ai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 把 AI 服务的原始错误翻译成命令行用户能看懂的一句话 + 建议动作。
 * <p>
 * 校对失败时，用户看到的往往是网关吐出的原始 JSON（例如
 * {@code {"error":{"message":"qoder: {\"pricingUrl\":...} (112/model_error)"}}}），
 * 完全不知道该干什么。这里针对常见情况给出中文解释。
 * <p>
 * 注意：分类只用于「改善提示」，绝不吞掉原始信息 —— 原文永远附在末尾，
 * 便于排查与向上游反馈。
 *
 * @author pcdd
 */
public final class AiErrors {

    private AiErrors() {
    }

    /**
     * HTTP 非 2xx 响应的友好消息。
     *
     * @param code HTTP 状态码
     * @param body 响应体（可能是 JSON，也可能是 HTML 错误页）
     * @param url  请求地址，用于提示本地网关是否启动
     */
    public static String describe(int code, String body, String url) {
        String msg = extractMessage(body);
        String hint = switch (code) {
            case 401 -> "密钥无效或未配置";
            case 403 -> "无权访问该模型（可能区域受限或账号未订阅）";
            case 404 -> "模型名或服务地址不存在，检查 model 与 base-url";
            case 429 -> "请求过于频繁，被限流；调低并发或加大 min-interval";
            case 502, 503 -> describeUpstream(msg);
            default -> code >= 500 ? "AI 服务暂时不可用" : "AI 服务拒绝了请求";
        };
        // 免费额度/未登录/限流这类「可操作」的信号优先于泛化 hint
        String actionable = actionable(msg);
        String head = StrUtil.isNotBlank(actionable) ? actionable : hint;
        return StrUtil.format("AI 服务返回 HTTP {}：{}｜原文：{}",
                code, head, StrUtil.maxLength(StrUtil.blankToDefault(msg, body), 200));
    }

    /**
     * 抛不出 HTTP 响应时的传输层异常（连不上、超时、DNS 失败）。
     */
    public static String describe(Throwable e, String url) {
        String cause = rootCause(e);
        if (e instanceof ConnectException || contains(cause, "connect", "refused", "failed to connect")) {
            return StrUtil.format("连不上 AI 服务地址 {}。若用的是 EAC 本地网关，"
                    + "请确认 DSH 的 Our Free Model 插件已启动（网关默认监听 127.0.0.1:18326）｜原文：{}",
                    url, StrUtil.maxLength(cause, 160));
        }
        if (e instanceof SocketTimeoutException || contains(cause, "timeout", "timed out")) {
            return StrUtil.format("请求 AI 服务超时（{}）。校对是长任务，可适当调大 timeout｜原文：{}",
                    url, StrUtil.maxLength(cause, 160));
        }
        if (e instanceof UnknownHostException || contains(cause, "unknownhost", "no such host", "name not resolved")) {
            return StrUtil.format("无法解析 AI 服务地址的域名（{}），检查 base-url 是否拼错｜原文：{}",
                    url, StrUtil.maxLength(cause, 160));
        }
        return StrUtil.format("请求 AI 服务失败：{}", StrUtil.maxLength(cause, 200));
    }

    /**
     * 上游 5xx 里最常见的三类：未登录、限流、账号被禁。
     */
    private static String describeUpstream(String msg) {
        String a = actionable(msg);
        return StrUtil.isNotBlank(a) ? a : "AI 服务/网关暂时不可用（上游无健康节点）";
    }

    /**
     * 从原始错误里识别「用户下一步该做什么」的信号。命中返回中文建议，否则空串。
     */
    private static String actionable(String msg) {
        if (StrUtil.isBlank(msg)) return "";
        String m = msg.toLowerCase();
        // EAC 国际版未登录 / 无可用账号
        if (contains(m, "no healthy upstream", "pricingurl", "model_error", "not logged in", "please login")) {
            return "该渠道账号未登录或无可用上游：EAC 国际版需先在 Qoder 登录，或改用国内版 provider=eac-qwen-cn";
        }
        // 账号限流（带解禁时间）
        if (contains(m, "限流", "解禁", "rate limit", "quota", "usage limit reached", "try again later")) {
            return "该模型账号正在限流/额度耗尽：稍后重试，或在 EAC 里换一个可用模型";
        }
        if (contains(m, "账号已被禁用", "account disabled", "banned")) {
            return "该渠道账号已被禁用：到 EAC 换一个已登录的账号或渠道";
        }
        if (contains(m, "not available in your country", "region", "country")) {
            return "该模型受区域限制，当前网络无法使用";
        }
        return "";
    }

    /**
     * 从响应体里抽 error.message；不是 JSON 就原样返回。
     */
    private static String extractMessage(String body) {
        if (StrUtil.isBlank(body) || !JSONUtil.isTypeJSON(body)) {
            return body;
        }
        try {
            JSONObject root = JSONUtil.parseObj(body);
            JSONObject err = root.getJSONObject("error");
            if (err != null) {
                String m = err.getStr("message");
                if (StrUtil.isNotBlank(m)) return m;
            }
            String m = root.getStr("message");
            if (StrUtil.isNotBlank(m)) return m;
        } catch (Exception ignore) {
            // 解析失败就退回原文
        }
        return body;
    }

    private static String rootCause(Throwable e) {
        Throwable t = e;
        StringBuilder sb = new StringBuilder();
        while (t != null) {
            if (StrUtil.isNotBlank(t.getMessage())) {
                if (!sb.isEmpty()) sb.append(" <- ");
                sb.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
            }
            if (t.getCause() == t) break;
            t = t.getCause();
        }
        return sb.isEmpty() ? (e == null ? "" : e.toString()) : sb.toString();
    }

    private static boolean contains(String haystack, String... needles) {
        if (haystack == null) return false;
        for (String n : needles) {
            if (!n.isEmpty() && haystack.contains(n)) return true;
        }
        return false;
    }

}
