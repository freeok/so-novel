package com.pcdd.sonovel.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.Setting;
import cn.hutool.setting.dialect.Props;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author pcdd
 */
@UtilityClass
public class Settings {

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
            Setting setting = new Setting(defaultPath);
            // 在配置文件变更时自动加载
            setting.autoLoad(true);
            // 若默认路径也不存在，则抛出 FileNotFoundException
            return setting;
        }

        Path absolutePath = Paths.get(configFilePath).toAbsolutePath();
        Setting setting = new Setting(absolutePath.toString());
        setting.autoLoad(true);

        return setting;
    }

}
