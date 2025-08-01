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

    private final String SELECTION_BASE = "base";
    private final String SELECTION_CRAWL = "crawl";
    private final String SELECTION_RETRY = "retry";
    private final String SELECTION_PROXY = "proxy";

    /**
     * 加载系统属性
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
        Props sys = sys();
        Setting usr = usr();

        AppConfig config = new AppConfig();
        config.setVersion(sys.getStr("version"));

        config.setLanguage(getStrOrDefault(usr, "language", SELECTION_BASE, LangUtil.getCurrentLang()));
        config.setActiveRules(getStrOrDefault(usr, "active-rules", SELECTION_BASE, "main-rules.json"));
        config.setDownloadPath(getStrOrDefault(usr, "download-path", SELECTION_BASE, "downloads"));
        // 扩展名一律转为小写
        config.setExtName(getStrOrDefault(usr, "extname", SELECTION_BASE, "epub").toLowerCase());
        config.setAutoUpdate(usr.getInt("auto-update", SELECTION_BASE, 0));
        config.setSourceId(usr.getInt("source-id", SELECTION_BASE, -1));
        config.setSearchLimit(usr.getInt("search-limit", SELECTION_BASE, 0));

        config.setThreads(usr.getInt("threads", SELECTION_CRAWL, -1));
        config.setMinInterval(usr.getInt("min", SELECTION_CRAWL, 200));
        config.setMaxInterval(usr.getInt("max", SELECTION_CRAWL, 400));
        config.setPreserveChapterCache(usr.getInt("preserve_chapter_cache", SELECTION_CRAWL, 0));

        config.setMaxRetryAttempts(usr.getInt("max-attempts", SELECTION_RETRY, 5));
        config.setRetryMinInterval(usr.getInt("min", SELECTION_RETRY, 2000));
        config.setRetryMaxInterval(usr.getInt("max", SELECTION_RETRY, 4000));

        config.setProxyEnabled(usr.getInt("enabled", SELECTION_PROXY, 0));
        config.setProxyHost(getStrOrDefault(usr, "host", SELECTION_PROXY, "127.0.0.1"));
        config.setProxyPort(usr.getInt("port", SELECTION_PROXY, 7890));

        return config;
    }

    // 修复 hutool 的 bug：空串不能触发默认值
    private String getStrOrDefault(Setting setting, String key, String group, String defaultValue) {
        String value = setting.getByGroup(key, group);
        return StrUtil.isEmpty(value) ? defaultValue : value;
    }

    public String resolveConfigFileName() {
        return EnvUtils.isDev() ? "config-dev.ini" : "config.ini";
    }

}