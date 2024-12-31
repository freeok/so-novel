package com.pcdd.sonovel.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.setting.Setting;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.ConfigBean;
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
            String defaultPath = System.getProperty("user.dir") + File.separator + "config.ini";
            // 若默认路径也不存在，则抛出 FileNotFoundException
            return new Setting(defaultPath);
        }

        Path absolutePath = Paths.get(configFilePath).toAbsolutePath();

        return new Setting(absolutePath.toString());
    }

    public ConfigBean config() {
        Props sys = sys();
        Setting usr = usr();

        ConfigBean configBean = new ConfigBean();
        configBean.setVersion(sys.getStr("version"));

        // 1 ~ 4
        configBean.setSourceId(usr.getInt("source-id", SELECTION_1, RandomUtil.randomInt(1, 5)));
        configBean.setDownloadPath(usr.getStr("download-path", SELECTION_1, "downloads"));
        configBean.setExtName(usr.getStr("extname", SELECTION_1, "epub"));
        configBean.setAutoUpdate(usr.getInt("auto-update", SELECTION_1, 1));
        configBean.setProxyHost(usr.getStr("proxy-host", SELECTION_1, "127.0.0.1"));
        configBean.setProxyPort(usr.getInt("proxy-port", SELECTION_1, 7890));

        configBean.setThreads(usr.getInt("threads", SELECTION_2, -1));
        configBean.setMinInterval(usr.getInt("min", SELECTION_2, 50));
        configBean.setMaxInterval(usr.getInt("max", SELECTION_2, 100));

        configBean.setMaxRetryAttempts(usr.getInt("max-attempts", SELECTION_3, 3));
        configBean.setRetryMinInterval(usr.getInt("min", SELECTION_3, 500));
        configBean.setRetryMaxInterval(usr.getInt("max", SELECTION_3, 2000));

        return configBean;
    }

}