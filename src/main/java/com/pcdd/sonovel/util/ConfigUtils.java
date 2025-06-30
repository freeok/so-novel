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

    public final String SELECTION_1 = "base";
    public final String SELECTION_2 = "crawl";
    public final String SELECTION_3 = "retry";
    public final String SELECTION_4 = "proxy";

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

        config.setLanguage(getStrOrDefault(usr, "language", SELECTION_1, LangUtil.getCurrentLang()));
        config.setActiveRules(getStrOrDefault(usr, "active-rules", SELECTION_1, "main-rules.json"));
        config.setDownloadPath(getStrOrDefault(usr, "download-path", SELECTION_1, "downloads"));
        // 扩展名一律转为小写
        config.setExtName(getStrOrDefault(usr, "extname", SELECTION_1, "epub").toLowerCase());

        config.setAutoUpdate(usr.getInt("auto-update", SELECTION_1, 0));
        config.setSourceId(usr.getInt("source-id", SELECTION_1, -1));
        config.setSearchLimit(usr.getInt("search-limit", SELECTION_1, 0));

        config.setThreads(usr.getInt("threads", SELECTION_2, -1));
        config.setMinInterval(usr.getInt("min", SELECTION_2, 200));
        config.setMaxInterval(usr.getInt("max", SELECTION_2, 400));
        config.setPreserveChapterCache(usr.getInt("preserve_chapter_cache", SELECTION_2, 0));
        config.setShowDownloadLog(usr.getInt("show_download_log", SELECTION_2, 1));

        config.setMaxRetryAttempts(usr.getInt("max-attempts", SELECTION_3, 5));
        config.setRetryMinInterval(usr.getInt("min", SELECTION_3, 2000));
        config.setRetryMaxInterval(usr.getInt("max", SELECTION_3, 4000));

        config.setProxyEnabled(usr.getInt("enabled", SELECTION_4, 0));
        config.setProxyHost(getStrOrDefault(usr, "host", SELECTION_4, "127.0.0.1"));
        config.setProxyPort(usr.getInt("port", SELECTION_4, 7890));

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