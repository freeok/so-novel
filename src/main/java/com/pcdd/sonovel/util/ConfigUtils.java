package com.pcdd.sonovel.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
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

    public AppConfig config() {
        Props sys = sys();
        Setting usr = usr();

        AppConfig config = new AppConfig();
        config.setVersion(sys.getStr("version"));

        config.setLanguage(usr.getStr("language", SELECTION_1, "zh_CN"));
        config.setDownloadPath(usr.getStr("download-path", SELECTION_1, "downloads"));
        // 扩展名一律转为小写
        config.setExtName(usr.getStr("extname", SELECTION_1, "epub").toLowerCase());
        config.setAutoUpdate(usr.getInt("auto-update", SELECTION_1, 0));
        config.setInteractiveMode(usr.getInt("interactive-mode", SELECTION_1, 1));
        config.setSourceId(usr.getInt("source-id", SELECTION_1, RandomUtil.randomInt(1, SourceUtils.getCount() + 1)));
        config.setSearchLimit(usr.getInt("search-limit", SELECTION_1, 0));

        config.setThreads(usr.getInt("threads", SELECTION_2, -1));
        config.setMinInterval(usr.getInt("min", SELECTION_2, 50));
        config.setMaxInterval(usr.getInt("max", SELECTION_2, 100));

        config.setMaxRetryAttempts(usr.getInt("max-attempts", SELECTION_3, 3));
        config.setRetryMinInterval(usr.getInt("min", SELECTION_3, 500));
        config.setRetryMaxInterval(usr.getInt("max", SELECTION_3, 2000));

        config.setProxyEnabled(usr.getInt("enabled", SELECTION_4, 0));
        config.setProxyHost(usr.getStr("host", SELECTION_4, "127.0.0.1"));
        config.setProxyPort(usr.getInt("port", SELECTION_4, 7890));

        return config;
    }

    public String resolveConfigFileName() {
        String env = System.getProperty("env", "dev").toLowerCase();
        return switch (env) {
            case "dev" -> "config-dev.ini";
            case "test" -> "config-test.ini";
            default -> "config.ini";
        };
    }

}