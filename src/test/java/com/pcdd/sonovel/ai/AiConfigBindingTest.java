package com.pcdd.sonovel.ai;

import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.AiConfig;
import com.pcdd.sonovel.model.AppConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * config.ini 的 [ai] 段到 {@link AiConfig} 的绑定测试。
 * <p>
 * INI 用 kebab-case（chunk-size），字段是 camelCase（chunkSize），靠 hutool 按 key 手工读取。
 * 这类映射最容易写错且<strong>错了不会报错</strong> —— 只会静默退回默认值，
 * 用户改了配置发现「没反应」也查不出来。故用真实 INI 文本走一遍加载链路。
 */
class AiConfigBindingTest {

    @TempDir
    Path tmp;

    /**
     * surefire 注入了默认测试配置（见 pom），本类用例会临时改指向。
     * 必须记下原值并在用例后恢复 —— 同 JVM 里后续测试仍要用它来初始化 AppConfigLoader，
     * 清掉会让它们退回「找不到配置文件」而连带报 NoClassDefFoundError。
     */
    private static final String ORIGINAL_CONFIG_FILE = System.getProperty("config.file");

    /**
     * 本机若设了 SONOVEL_AI_KEY，它会压过文件与启动参数，属于预期优先级；
     * 这些用例只在「没有外部密钥」的前提下断言可用性
     */
    private static final boolean HAS_ENV_KEY =
            System.getenv("SONOVEL_AI_KEY") != null && !System.getenv("SONOVEL_AI_KEY").isBlank();

    @AfterEach
    void restore() {
        if (ORIGINAL_CONFIG_FILE != null) {
            System.setProperty("config.file", ORIGINAL_CONFIG_FILE);
        } else {
            System.clearProperty("config.file");
        }
        System.clearProperty("ai.api-key");
        // 恢复到指向真实测试文件的缓存状态
        AppConfigLoader.reload();
    }

    private AppConfig load(String ini) throws Exception {
        Path f = tmp.resolve("config.ini");
        Files.writeString(f, ini, StandardCharsets.UTF_8);
        System.setProperty("config.file", f.toAbsolutePath().toString());
        // 必须 reload：hutool Setting 会把文件内容缓进内存，不清缓存读到的还是上一个文件
        AppConfigLoader.reload();
        return AppConfigLoader.loadConfig();
    }

    /**
     * 所有键都应按名字精确落到字段上，而不是退回默认值
     */
    @Test
    void readsAllAiKeys() throws Exception {
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                fix-garbled = 0
                base-url = https://example.com/v1
                model = my-model
                timeout = 42
                max-tokens = 777
                chunk-size = 900
                max-terms = 55
                min-interval = 123
                concurrency = 7
                audit = 0
                """);

        AiConfig ai = cfg.getAi();
        assertEquals(1, ai.getEnabled());
        assertEquals(0, ai.getFixGarbled());
        assertEquals("https://example.com/v1", ai.getBaseUrl());
        assertEquals("my-model", ai.getModel());
        assertEquals(42, ai.getTimeout());
        assertEquals(777, ai.getMaxTokens());
        assertEquals(900, ai.getChunkSize());
        assertEquals(55, ai.getMaxTerms());
        assertEquals(123, ai.getMinInterval());
        assertEquals(7, ai.getConcurrency());
        assertEquals(0, ai.getAudit());
        if (!HAS_ENV_KEY) {
            assertFalse(ai.usable(), "没给密钥不得判定为可用");
        }
    }

    /**
     * 缺省即关闭：没有 [ai] 段时绝不能意外开启（会悄悄把正文发给第三方）
     */
    @Test
    void disabledByDefaultWhenSectionMissing() throws Exception {
        AppConfig cfg = load("""
                [download]
                extname = txt
                """);

        AiConfig ai = cfg.getAi();
        assertNotNull(ai, "缺 [ai] 段也必须给出默认对象，不得为 null");
        assertEquals(0, ai.getEnabled());
        assertFalse(ai.usable());
        assertFalse(ai.garbledFixEnabled());
    }

    /**
     * 密钥优先级：环境变量 > 启动参数 > 配置文件
     */
    @Test
    void systemPropertyOverridesIni() throws Exception {
        System.setProperty("ai.api-key", "sk-from-jvm");
        try {
            AppConfig cfg = load("""
                    [ai]
                    enabled = 1
                    base-url = https://example.com/v1
                    api-key = sk-from-ini
                    """);
            // 本机若恰好设置了环境变量 SONOVEL_AI_KEY，它会压过启动参数，属预期行为
            String expected = HAS_ENV_KEY ? System.getenv("SONOVEL_AI_KEY") : "sk-from-jvm";
            assertEquals(expected, cfg.getAi().getApiKey());
            assertTrue(cfg.getAi().usable());
        } finally {
            System.clearProperty("ai.api-key");
        }
    }

    /**
     * 掩码占位符不是密钥：即便被写进文件也不能让配置变成「可用」
     */
    @Test
    void maskIsNotTreatedAsKey() throws Exception {
        // 本机环境变量/启动参数的真实密钥会顶替文件值，该用例只针对「文件里是掩码」的场景
        Assumptions.assumeFalse(HAS_ENV_KEY || System.getProperty("ai.api-key") != null,
                "存在外部真实密钥时跳过");
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                base-url = https://example.com/v1
                api-key = ********
                """);
        AiConfig ai = cfg.getAi();
        assertFalse(ai.hasKey(), "掩码不得被当成真实密钥");
        assertFalse(ai.usable());
    }

    /**
     * 空值的键要落到默认值而不是空串（hutool 对空串不触发默认值的坑）
     */
    @Test
    void blankValuesFallBackToDefaults() throws Exception {
        AppConfig cfg = load("""
                [ai]
                enabled = 1
                base-url =
                model =
                """);
        AiConfig ai = cfg.getAi();
        assertEquals("", ai.getBaseUrl());
        assertEquals("deepseek-chat", ai.getModel(), "model 留空应回默认模型名");
        assertFalse(ai.usable());
    }

    /**
     * 其它配置段的读取不受新增 [ai] 影响
     */
    @Test
    void otherSectionsStillLoad() throws Exception {
        AppConfig cfg = load("""
                [download]
                extname = txt
                [ai]
                enabled = 1
                """);
        assertEquals("txt", cfg.getExtName());
        assertEquals(1, cfg.getAi().getEnabled());
    }

}
