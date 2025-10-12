package com.pcdd.sonovel.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.setting.Setting;
import com.pcdd.sonovel.launch.TuiLauncher;
import com.pcdd.sonovel.model.AppConfig;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Paths;

@UtilityClass
public class ConfigWatcher {

    // 被赋值的属性不能是 static
    @Getter
    private AppConfig config = ConfigUtils.defaultConfig();

    public void watch() {
        String configFilePath = System.getProperty("config.file");
        String path;

        if (!FileUtil.exist(configFilePath)) {
            path = System.getProperty("user.dir") + File.separator + ConfigUtils.resolveConfigFileName();
        } else {
            path = Paths.get(configFilePath).toAbsolutePath().toString();
        }

        // 在配置文件变更时自动加载
        new Setting(path).autoLoad(true, aBoolean -> {
            config = ConfigUtils.defaultConfig();
            Console.log("\n<== 监听到配置文件变更，开始热加载配置");
            TuiLauncher.printHint(config);
        });
    }

}