package com.pcdd.sonovel.model;

import lombok.Data;

/**
 * AI 校对功能配置（config.ini 的 [ai] 段）。
 * <p>
 * 默认全部关闭：该功能会向第三方 AI 服务发送正文内容，必须由用户显式开启并自行填写密钥。
 *
 * @author pcdd
 */
@Data
public class AiConfig {

    /**
     * 密钥掩码占位符（单一真源）。
     * 下发给前端时用它占位；读取配置时遇到它一律视为「未配置」，
     * 防止占位符被当成真实密钥拿去请求 AI 服务。
     */
    public static final String SECRET_MASK = "********";

    /**
     * 总开关 (1 开，0 关)
     */
    private Integer enabled = 0;

    /**
     * OpenAI 兼容服务地址，例如 https://api.deepseek.com/v1
     */
    private String baseUrl = "";

    /**
     * 密钥。可写 config.ini，也可用环境变量 SONOVEL_AI_KEY 覆盖（优先，避免密钥落盘）
     */
    private String apiKey = "";

    /**
     * 模型名，例如 deepseek-chat / qwen-plus / gpt-4o-mini
     */
    private String model = "deepseek-chat";

    /**
     * 单次请求超时（秒）。AI 响应远慢于爬虫，默认 60
     */
    private Integer timeout = 60;

    /**
     * 单次响应 token 上限
     */
    private Integer maxTokens = 1200;

    /**
     * 单次送 AI 的最大字符数（按段落累加，超限则切分为多次请求）
     */
    private Integer chunkSize = 1200;

    /**
     * 背景调研词表上限（保护名单长度）
     */
    private Integer maxTerms = 200;

    /**
     * 两章之间的最小间隔（毫秒），避免被 AI 服务限流
     */
    private Integer minInterval = 300;

    /**
     * 校对并发数（AI 服务通常限并发，默认 4）
     */
    private Integer concurrency = 4;

    /**
     * 是否先做确定性乱码修复（不花 token，默认开）
     */
    private Integer fixGarbled = 1;

    /**
     * 是否输出逐条改动审计文件（proofread.log），默认开
     */
    private Integer audit = 1;

    /**
     * AI 服务预设名（留空=自定义）。内置预设自动补齐地址与模型，
     * 适合「不想手填 OpenAI 兼容端点」的场景。
     * <p>
     * 注意：预设只填「用户没显式写的」字段，显式值优先 —— 这样改预设不会
     * 覆盖用户已填好的地址，也不会因为预设失效而把已配置的服务弄丢。
     */
    private String provider = "";

    /**
     * 预设服务的密钥来源文件（例如本机 EAC 网关的 settings.json）。
     * 默认 {@code ~/.dsh/our-free-model/settings.json}。
     * 读不到时退回常规的 api-key / 环境变量 / 启动参数。
     */
    private String eacKeyFile = "";

    /**
     * 是否让模型开启「思考」。
     * <p>
     * 校对是确定性任务，不需要推理链。实测同一条请求：开思考时
     * reasoning tokens 占到 completion 的 83%~92%（例如 763 里有 635），
     * 关掉后整体 token 降 3.8 倍、耗时降 2~3 倍。所以默认关。
     * <p>
     * 仅对「有 provider 预设」的服务发送该字段，避免给不接受此参数的
     * OpenAI 兼容服务添麻烦。
     */
    private Integer enableThinking = 0;

    /**
     * 是否要求配置完整才启用（无 key 时自动降级为「只修乱码不调 AI」）
     */
    public boolean usable() {
        return enabled != null && enabled == 1
                && baseUrl != null && !baseUrl.isBlank()
                && hasKey()
                && model != null && !model.isBlank();
    }

    /**
     * 是否使用内置预设服务（决定 LlmClient 是否发送 enable_thinking）。
     * <p>
     * 必须是<b>已知</b>预设：未知名虽然被选了，但我们不知道它背后是什么服务，
     * 不该给它乱发思考开关 —— 拿一个不支持该字段的自定义服务当预设用，
     * 客户端会被 400 拒绝，比不发更糟。
     */
    public boolean isProviderBased() {
        return provider != null && !provider.isBlank() && AiPresets.isKnown(provider);
    }

    /**
     * 是否真的拿到了可用密钥（掩码占位符不算）
     */
    public boolean hasKey() {
        return apiKey != null && !apiKey.isBlank() && !SECRET_MASK.equals(apiKey.trim());
    }

    /**
     * 是否至少要做确定性乱码修复
     */
    public boolean garbledFixEnabled() {
        return enabled != null && enabled == 1 && fixGarbled != null && fixGarbled == 1;
    }

}
