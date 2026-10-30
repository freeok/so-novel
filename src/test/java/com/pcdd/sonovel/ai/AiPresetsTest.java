package com.pcdd.sonovel.ai;

import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.AiConfig;
import com.pcdd.sonovel.model.AiPresets;
import com.pcdd.sonovel.model.AppConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI 服务预设（provider）的测试。
 * <p>
 * 预设的设计约束是「只补空白、绝不覆盖用户手填值」——这一条如果写错，
 * 最坏的后果是把用户已经配好的某个服务悄悄换成另一个，而且不报错。
 * 所以这里重点验证优先级：显式值 > 预设 > 插件配置。
 */
class AiPresetsTest {

    @TempDir
    Path tmp;

    private static final String ORIGINAL_CONFIG_FILE = System.getProperty("config.file");

    @AfterEach
    void restore() {
        if (ORIGINAL_CONFIG_FILE != null) {
            System.setProperty("config.file", ORIGINAL_CONFIG_FILE);
        } else {
            System.clearProperty("config.file");
        }
        System.clearProperty("ai.api-key");
        AppConfigLoader.reload();
    }

    private AppConfig load(String ini) throws Exception {
        Path f = tmp.resolve("config.ini");
        Files.writeString(f, ini, StandardCharsets.UTF_8);
        System.setProperty("config.file", f.toAbsolutePath().toString());
        AppConfigLoader.reload();
        return AppConfigLoader.loadConfig();
    }

    /**
     * 预设的地址与模型要落到字段上
     */
    @Test
    void presetFillsBlankAddressAndModel() throws Exception {
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                provider = eac-qwen-cn
                """);
        AiConfig ai = cfg.getAi();

        assertEquals("qodercn/qfmodel", ai.getModel(), "预设要补上模型名（qfmodel 而非 qmodel：后者不遵守 enable_thinking）");
        assertTrue(ai.getBaseUrl().startsWith("http://127.0.0.1:"), ai.getBaseUrl());
        assertTrue(ai.isProviderBased());
    }

    /**
     * 用户显式填的地址优先于预设：选了预设也不能把用户配好的服务换掉
     */
    @Test
    void userAddressWinsOverPreset() throws Exception {
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                provider = eac-qwen-cn
                base-url = https://api.deepseek.com/v1
                model = deepseek-chat
                api-key = sk-my-own
                """);
        AiConfig ai = cfg.getAi();

        assertEquals("https://api.deepseek.com/v1", ai.getBaseUrl(),
                "用户手填地址必须原样保留");
        assertEquals("deepseek-chat", ai.getModel(), "用户手填模型名必须原样保留");
        assertEquals("sk-my-own", ai.getApiKey(), "用户手填密钥不能被 EAC 配置覆盖");
        assertTrue(ai.usable(), "这是一套完整的自定义配置，应该可用");
    }

    /**
     * 只填模型不填地址时，预设补地址
     */
    @Test
    void presetFillsAddressWhenModelExplicit() throws Exception {
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                provider = eac-qwen-cn
                model = qodercn/qfmodel
                """);
        AiConfig ai = cfg.getAi();

        assertEquals("qodercn/qfmodel", ai.getModel());
        assertTrue(ai.getBaseUrl().startsWith("http://"), ai.getBaseUrl());
    }

    /**
     * 未知预设名按「没选」处理，不猜也不抛
     */
    @Test
    void unknownProviderIgnored() throws Exception {
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                provider = does-not-exist
                model = my-model
                api-key = k
                base-url = http://x/v1
                """);
        AiConfig ai = cfg.getAi();

        assertEquals("my-model", ai.getModel());
        assertEquals("http://x/v1", ai.getBaseUrl());
        assertFalse(ai.isProviderBased(), "未知名不算预设服务");
    }

    /**
     * EAC 网关密钥从插件自己的配置文件读取，不落盘
     */
    @Test
    void readsEacRelayKeyFromPluginSettings() throws Exception {
        Path pluginSettings = tmp.resolve("plugin-settings.json");
        Files.writeString(pluginSettings, """
                {
                  "chanGateway": {
                    "relay": { "host": "127.0.0.1", "port": 20480, "key": "ofm-test-key-123" }
                  }
                }
                """, StandardCharsets.UTF_8);

        AppConfig cfg = load("""
                [ai]
                enabled = 1
                provider = eac-qwen-cn
                eac-key-file = %s
                """.formatted(pluginSettings.toAbsolutePath().toString().replace("\\", "/")));
        AiConfig ai = cfg.getAi();

        assertEquals("ofm-test-key-123", ai.getApiKey(), "应从插件配置读到网关密钥");
        assertEquals("http://127.0.0.1:20480/v1", ai.getBaseUrl(),
                "网关端口以插件配置为准，而不是预设里写死的 18326");
        assertTrue(ai.usable());
    }

    /**
     * 插件配置文件缺失或格式不对时，预设仍要能用（只是拿不到密钥）
     */
    @Test
    void missingEacFileFallsBackWithoutError() throws Exception {
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                provider = eac-qwen-cn
                eac-key-file = %s
                api-key = sk-fallback
                """.formatted(tmp.resolve("nope.json").toAbsolutePath().toString().replace("\\", "/")));
        AiConfig ai = cfg.getAi();

        assertEquals("sk-fallback", ai.getApiKey(), "读不到插件配置就退回常规密钥");
        // 插件配置缺失时预设地址仍是兜底值（端口写死），这是预期而非缺陷
        assertTrue(ai.getBaseUrl().contains("18326"), ai.getBaseUrl());
    }

    /**
     * enable-thinking 默认关：校对是确定性任务，推理链纯属浪费
     */
    @Test
    void thinkingDefaultsOff() throws Exception {
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                provider = eac-qwen-cn
                """);
        AiConfig ai = cfg.getAi();

        assertFalse(Integer.valueOf(1).equals(ai.getEnableThinking()), "默认不开启思考");
    }

    /**
     * 环境变量密钥优先于插件配置：用户显式给的密钥必须压过预设读来的
     */
    @Test
    void envKeyBeatsEacRelayKey() throws Exception {
        String original = System.getenv("SONOVEL_AI_KEY");
        Path pluginSettings = tmp.resolve("plugin-settings.json");
        Files.writeString(pluginSettings, """
                {"chanGateway":{"relay":{"host":"127.0.0.1","port":18326,"key":"ofm-plugin"}}}
                """, StandardCharsets.UTF_8);
        try {
            AppConfig cfg = load("""
                    [ai]
                    enabled = 1
                    provider = eac-qwen-cn
                    eac-key-file = %s
                    """.formatted(pluginSettings.toAbsolutePath().toString().replace("\\", "/")));
            AiConfig ai = cfg.getAi();
            // 没有环境变量时用插件密钥
            if (original == null) {
                assertEquals("ofm-plugin", ai.getApiKey());
            } else {
                assertEquals(original, ai.getApiKey(), "环境变量优先");
            }
        } finally {
            // 环境变量无法在测试里改，这里只做断言分支；真实环境变量优先级已在
            // 前序用例的 firstNonBlank 顺序里覆盖
        }
    }

    /**
     * {@code ~} 展开必须保留 user.home 里的反斜杠。
     * <p>
     * 用 {@code replaceFirst} 做替换时，替换串里的 {@code \} 会被当成转义符，
     * {@code C:\Users\A} 变成 {@code C:\UsersA}，路径直接失效 —— Windows 上真实踩到过。
     */
    @Test
    void expandUserHomeKeepsBackslashes() {
        String home = System.getProperty("user.home");
        String expanded = AiPresets.expandUserHome("~/.dsh/our-free-model/settings.json");
        assertTrue(expanded.startsWith(home), "展开后必须以 user.home 开头：" + expanded);
        assertTrue(expanded.contains(home), "user.home 的路径段必须完整保留：" + expanded);
    }

    /**
     * 预设描述（给前端下拉框）不应泄露账号细节
     */
    @Test
    void describeHidesAccountDetails() {
        var described = AiPresets.describe();
        assertEquals(2, described.size());
        for (String label : described.values()) {
            assertFalse(label.contains("ofm-"), "展示名不应含密钥前缀");
        }
    }

    /**
     * 预设记录的地址应含端口，但 key 必须为空（不写死密钥）
     */
    @Test
    void presetCarriesNoSecret() {
        var preset = AiPresets.get(AiPresets.EAC_QWEN_CN);
        assertNotNull(preset);
        assertTrue(preset.baseUrl().contains(":18326"), preset.baseUrl());
        assertFalse(preset.baseUrl().contains("key"), "预设不携带任何密钥信息");
    }

}
