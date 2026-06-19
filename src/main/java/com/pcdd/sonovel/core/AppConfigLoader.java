package com.pcdd.sonovel.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.util.EnvUtils;
import com.pcdd.sonovel.util.FileUtils;
import com.pcdd.sonovel.util.LangUtil;
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
        cfg.setLanguage(getStrOrDefault(setting, "language", SELECTION_SOURCE, LangUtil.getCurrentLang()));
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

        return cfg;
    }

    // 修复 hutool 空串不能触发默认值的 bug
    private String getStrOrDefault(Setting setting, String key, String group, String defaultValue) {
        String value = setting.getByGroup(key, group);
        return StrUtil.isEmpty(value) ? defaultValue : value;
    }

    private String resolveConfigFileName() {
        return FileUtils.toAbsolutePath(EnvUtils.isDev() ? "config-dev.ini" : "config.ini");
    }

}