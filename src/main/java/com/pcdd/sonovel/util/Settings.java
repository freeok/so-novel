package com.pcdd.sonovel.util;

import cn.hutool.setting.dialect.Props;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author pcdd
 */
@UtilityClass
public class Settings {

    public Props sys() {
        return Props.getProp("application.ini", StandardCharsets.UTF_8);
    }

    public Props usr() {
        // classpath 下的配置文件用户无法修改，因为已经打包进 jar
        return Props.getProp(System.getProperty("user.dir") + File.separator + "config.ini", StandardCharsets.UTF_8);
    }

}