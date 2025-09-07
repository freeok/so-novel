package com.pcdd.sonovel.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.AppConfig;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author pcdd
 * Created at 2024/3/23
 */
@UtilityClass
public class ConfigUtils {

    private final String SELECTION_GLOBAL = "global";
    private final String SELECTION_DOWNLOAD = "download";
    private final String SELECTION_SOURCE = "source";
    private final String SELECTION_CRAWL = "crawl";
    private final String SELECTION_WEB = "web";
    private final String SELECTION_COOKIE = "cookie";
    private final String SELECTION_PROXY = "proxy";

    /**
     * 加载应用属性
     */
    public Props sys() {
        return Props.getProp("application.properties", StandardCharsets.UTF_8);
    }

    /**
     * 加载用户属性
     */
    public Setting usr() {
        // 从虚拟机选项 -Dconfig.file 获取用户配置文件路径
        String configFilePath = System.getProperty("config.file");

        // 若未指定或指定路径不存在，则从默认位置获取
        if (!FileUtil.exist(configFilePath)) {
            // 用户配置文件默认路径
            String defaultPath = System.getProperty("user.dir") + File.separator + resolveConfigFileName();
            // 若默认路径也不存在，则抛出 FileNotFoundException
            return new Setting(defaultPath);
        }

        Path absolutePath = Paths.get(configFilePath).toAbsolutePath();

        return new Setting(absolutePath.toString());
    }

    public AppConfig defaultConfig() {
        AppConfig config = new AppConfig();
        config.setVersion(sys().getStr("version"));
        Setting usr = usr();

        // [global]
        config.setAutoUpdate(usr.getInt("auto-update", SELECTION_GLOBAL, 0));
        config.setGhProxy(usr.getStr("gh-proxy", SELECTION_GLOBAL, ""));

        // [download]
        config.setDownloadPath(getStrOrDefault(usr, "download-path", SELECTION_DOWNLOAD, "downloads"));
        config.setExtName(getStrOrDefault(usr, "extname", SELECTION_DOWNLOAD, "epub").toLowerCase());
        config.setPreserveChapterCache(usr.getInt("preserve-chapter-cache", SELECTION_DOWNLOAD, 0));

        // [source]
        config.setLanguage(getStrOrDefault(usr, "language", SELECTION_SOURCE, LangUtil.getCurrentLang()));
        config.setActiveRules(getStrOrDefault(usr, "active-rules", SELECTION_SOURCE, "main-rules.json"));
        config.setSourceId(usr.getInt("source-id", SELECTION_SOURCE, -1));
        config.setSearchLimit(usr.getInt("search-limit", SELECTION_SOURCE, 0));

        // [crawl]
        config.setThreads(usr.getInt("threads", SELECTION_CRAWL, -1));
        config.setMinInterval(usr.getInt("min-interval", SELECTION_CRAWL, 200));
        config.setMaxInterval(usr.getInt("max-interval", SELECTION_CRAWL, 400));
        config.setEnableRetry(usr.getInt("enable-retry", SELECTION_CRAWL, 1));
        config.setMaxRetries(usr.getInt("max-retries", SELECTION_CRAWL, 5));
        config.setRetryMinInterval(usr.getInt("retry-min-interval", SELECTION_CRAWL, 2000));
        config.setRetryMaxInterval(usr.getInt("retry-max-interval", SELECTION_CRAWL, 4000));

        // [web]
        config.setWebEnabled(usr.getInt("enabled", SELECTION_WEB, 0));
        config.setWebPort(usr.getInt("port", SELECTION_WEB, 7765));

        // [cookie]
        config.setQidianCookie(usr.getStr("qidian", SELECTION_COOKIE, ""));

        // [proxy]
        config.setProxyEnabled(usr.getInt("enabled", SELECTION_PROXY, 0));
        config.setProxyHost(getStrOrDefault(usr, "host", SELECTION_PROXY, "127.0.0.1"));
        config.setProxyPort(usr.getInt("port", SELECTION_PROXY, 7890));

        return config;
    }

    // 修复 hutool 空串不能触发默认值的 bug
    private String getStrOrDefault(Setting setting, String key, String group, String defaultValue) {
        String value = setting.getByGroup(key, group);
        return StrUtil.isEmpty(value) ? defaultValue : value;
    }

    public String resolveConfigFileName() {
        return EnvUtils.isDev() ? "config-dev.ini" : "config.ini";
    }

}