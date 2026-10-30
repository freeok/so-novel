package com.pcdd.sonovel.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.AiConfig;
import com.pcdd.sonovel.model.AiPresets;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.utils.EnvUtils;
import com.pcdd.sonovel.utils.FileUtils;
import com.pcdd.sonovel.utils.LangUtils;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * @author pcdd
 * Created at 2024/3/23
 */
@UtilityClass
public class AppConfigLoader {

    private final String SELECTION_GLOBAL = "global";
    private final String SELECTION_DOWNLOAD = "download";
    private final String SELECTION_SOURCE = "source";
    private final String SELECTION_CRAWL = "crawl";
    private final String SELECTION_WEB = "web";
    private final String SELECTION_COOKIE = "cookie";
    private final String SELECTION_PROXY = "proxy";
    private final String SELECTION_AI = "ai";
    public final AppConfig APP_CONFIG = loadConfig();
    private static volatile Setting cachedSetting;

    /**
     * 加载应用属性
     */
    public Props sys() {
        return Props.getProp("application.properties", StandardCharsets.UTF_8);
    }

    /**
     * 加载用户属性，缓存 Setting 避免重复 I/O
     */
    public Setting usr() {
        Setting s = cachedSetting;
        if (s != null) return s;

        // 从虚拟机选项 -Dconfig.file 获取用户配置文件路径
        String configFilePath = System.getProperty("config.file");

        // 若未指定或指定路径不存在，则从默认位置获取
        if (StrUtil.isBlank(configFilePath) || !FileUtil.exist(configFilePath)) {
            // 用户配置文件默认路径
            String defaultPath = resolveConfigFileName();
            // 若默认路径也不存在，则抛出 FileNotFoundException
            cachedSetting = new Setting(defaultPath);
            return cachedSetting;
        }

        cachedSetting = new Setting(Paths.get(configFilePath).toAbsolutePath().toString());
        return cachedSetting;
    }

    /**
     * 使配置文件缓存失效。
     * <p>
     * hutool 的 Setting 会把内容读进内存，缓存后再次 new Setting 才会重读磁盘。
     * 只有<strong>改过 config.ini 之后</strong>必须调用本方法（见 ConfigService#save），
     * 否则 loadConfig() 拿到的仍是旧值。
     * <p>
     * 注意：不要在 loadConfig() 里自动清缓存 —— SourceUtils 会为每个书源各调一次
     * loadConfig()，那样会把这层缓存整个废掉，退化成每次重读磁盘。
     */
    public void reload() {
        cachedSetting = null;
    }

    public AppConfig loadConfig() {
        AppConfig cfg = new AppConfig();
        cfg.setVersion(sys().getStr("version"));
        Setting setting = usr();

        // [global]
        cfg.setAutoUpdate(setting.getInt("auto-update", SELECTION_GLOBAL, 0));
        cfg.setGhProxy(setting.getStr("gh-proxy", SELECTION_GLOBAL, ""));
        cfg.setCfBypass(setting.getStr("cf-bypass", SELECTION_GLOBAL, null));

        // [download]
        cfg.setDownloadPath(getStrOrDefault(setting, "download-path", SELECTION_DOWNLOAD, "downloads"));
        cfg.setExtName(getStrOrDefault(setting, "extname", SELECTION_DOWNLOAD, "epub").toLowerCase());
        cfg.setTxtEncoding(getStrOrDefault(setting, "txt-encoding", SELECTION_DOWNLOAD, "UTF-8"));
        cfg.setPreserveChapterCache(setting.getInt("preserve-chapter-cache", SELECTION_DOWNLOAD, 0));
        cfg.setEnableProgressbar(setting.getInt("enable-progressbar", SELECTION_DOWNLOAD, 1));

        // [source]
        cfg.setLanguage(getStrOrDefault(setting, "language", SELECTION_SOURCE, LangUtils.getCurrentLang()));
        cfg.setActiveRules(getStrOrDefault(setting, "active-rules", SELECTION_SOURCE, "main.json"));
        cfg.setSourceId(setting.getInt("source-id", SELECTION_SOURCE, -1));
        cfg.setSearchLimit(setting.getInt("search-limit", SELECTION_SOURCE, -1));
        cfg.setSearchFilter(setting.getInt("search-filter", SELECTION_SOURCE, 1));

        // [crawl]
        cfg.setConcurrency(setting.getInt("concurrency", SELECTION_CRAWL, -1));
        cfg.setMinInterval(setting.getInt("min-interval", SELECTION_CRAWL, 200));
        cfg.setMaxInterval(setting.getInt("max-interval", SELECTION_CRAWL, 400));
        cfg.setEnableRetry(setting.getInt("enable-retry", SELECTION_CRAWL, 1));
        cfg.setMaxRetries(setting.getInt("max-retries", SELECTION_CRAWL, 5));
        cfg.setRetryMinInterval(setting.getInt("retry-min-interval", SELECTION_CRAWL, 2000));
        cfg.setRetryMaxInterval(setting.getInt("retry-max-interval", SELECTION_CRAWL, 4000));

        // [web]
        String mode = System.getProperty("mode", "tui");
        if ("web".equalsIgnoreCase(mode)) {
            cfg.setWebEnabled(1);
        } else {
            cfg.setWebEnabled(setting.getInt("enabled", SELECTION_WEB, 0));
        }
        cfg.setWebPort(setting.getInt("port", SELECTION_WEB, 7765));

        // [cookie]
        cfg.setQidianCookie(setting.getStr("qidian", SELECTION_COOKIE, ""));

        // [proxy]
        cfg.setProxyEnabled(setting.getInt("enabled", SELECTION_PROXY, 0));
        cfg.setProxyHost(getStrOrDefault(setting, "host", SELECTION_PROXY, "127.0.0.1"));
        cfg.setProxyPort(setting.getInt("port", SELECTION_PROXY, 7890));

        // [ai]
        cfg.setAi(loadAi(setting));

        return cfg;
    }

    /**
     * 读取 AI 校对配置。
     * 密钥允许三种来源，优先级从高到低：环境变量 SONOVEL_AI_KEY &gt; JVM 参数 -Dai.api-key &gt; config.ini。
     * 这样使用者可以不必把密钥写进明文配置文件（避免误提交/误分享）。
     */
    private AiConfig loadAi(Setting setting) {
        AiConfig ai = new AiConfig();
        ai.setEnabled(setting.getInt("enabled", SELECTION_AI, 0));
        ai.setBaseUrl(StrUtil.trimToEmpty(setting.getStr("base-url", SELECTION_AI, "")));
        /**
         * 记下用户是否显式写了地址：预设只补空白，插件配置更不能覆盖它。
         * 用户手填的地址优先，否则「换了个本地网关就把我配好的服务换掉」这种
         * 事故会变成常态。
         */
        boolean userSetBaseUrl = !ai.getBaseUrl().isBlank();
        /**
         * model 要先取原始值：getStrOrDefault 会把「空串」也当缺失并填默认模型名，
         * 那样预设就永远看不到「用户没填」这个事实，从而补不上预设的模型。
         */
        ai.setModel(StrUtil.trimToEmpty(setting.getStr("model", SELECTION_AI, "")));
        ai.setTimeout(setting.getInt("timeout", SELECTION_AI, 60));
        ai.setMaxTokens(setting.getInt("max-tokens", SELECTION_AI, 1200));
        ai.setChunkSize(setting.getInt("chunk-size", SELECTION_AI, 1200));
        ai.setMaxTerms(setting.getInt("max-terms", SELECTION_AI, 200));
        ai.setMinInterval(setting.getInt("min-interval", SELECTION_AI, 300));
        ai.setConcurrency(setting.getInt("concurrency", SELECTION_AI, 4));
        ai.setFixGarbled(setting.getInt("fix-garbled", SELECTION_AI, 1));
        ai.setAudit(setting.getInt("audit", SELECTION_AI, 1));
        ai.setProvider(StrUtil.trimToEmpty(setting.getStr("provider", SELECTION_AI, "")));
        ai.setEacKeyFile(StrUtil.trimToEmpty(setting.getStr("eac-key-file", SELECTION_AI, "")));
        ai.setEnableThinking(setting.getInt("enable-thinking", SELECTION_AI, 0));

        String key = firstNonBlank(
                System.getenv("SONOVEL_AI_KEY"),
                System.getProperty("ai.api-key"),
                setting.getStr("api-key", SELECTION_AI, ""));
        // 掩码占位符不是密钥：万一被写进文件（老版本或被手工粘贴），不能让拿去请求服务
        if (AiConfig.SECRET_MASK.equals(StrUtil.trimToEmpty(key))) {
            key = "";
        }

        if (ai.isProviderBased() && AiPresets.isKnown(ai.getProvider())) {
            AiPresets.Preset preset = AiPresets.get(ai.getProvider());
            // 预设只补空白：用户显式填过的字段一律优先
            if (StrUtil.isBlank(ai.getBaseUrl())) ai.setBaseUrl(preset.baseUrl());
            if (StrUtil.isBlank(ai.getModel())) ai.setModel(preset.model());

            /**
             * 只要用户自己填过地址，就当他在用自定义端点：
             * 不再读插件的密钥、也不用插件的端口。
             * 否则「用户手填了 deepseek 的地址 + 顺手选了个预设」会变成
             * 拿 EAC 的 key 去请求 deepseek —— 报 401 还很难看出原因。
             */
            if (userSetBaseUrl) {
                if (StrUtil.isBlank(ai.getModel())) {
                    ai.setModel("deepseek-chat");
                }
            } else {
                // EAC 这类本地网关的密钥由插件自己管理：读它自己的配置文件。
                // 读不到就退回上面的常规来源，绝不因为预设而报错。
                AiPresets.Relay relay = AiPresets.readEacRelay(ai.getEacKeyFile());
                if (StrUtil.isBlank(key) && relay != null) {
                    key = relay.key();
                }
                // 本地网关端口以插件配置为准，而不是预设里写死的端口
                if (relay != null) {
                    ai.setBaseUrl(relay.baseUrl());
                }
            }
        } else if (StrUtil.isBlank(ai.getModel())) {
            // 没选预设、也没手写模型时，沿用历史默认值
            ai.setModel("deepseek-chat");
        }

        ai.setApiKey(key == null ? "" : key.trim());
        return ai;
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (StrUtil.isNotBlank(v)) return v;
        }
        return null;
    }

    // 修复 hutool 空串不能触发默认值的 bug
    private String getStrOrDefault(Setting setting, String key, String group, String defaultValue) {
        String value = setting.getByGroup(key, group);
        return StrUtil.isBlank(value) ? defaultValue : value;
    }

    /**
     * 获取用户配置文件的绝对路径（供 Web 配置可视化在写回时备份/行级编辑）
     */
    public String getConfigFilePath() {
        String configFilePath = System.getProperty("config.file");
        if (StrUtil.isNotBlank(configFilePath) && FileUtil.exist(configFilePath)) {
            return Paths.get(configFilePath).toAbsolutePath().toString();
        }
        return resolveConfigFileName();
    }

    private String resolveConfigFileName() {
        return FileUtils.toAbsolutePath(EnvUtils.isDev() ? "config-dev.ini" : "config.ini");
    }

}