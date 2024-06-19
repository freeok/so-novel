package com.pcdd.sonovel.util;

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

    public Props sys() {
        return Props.getProp("application.properties", StandardCharsets.UTF_8);
    }

    public Props usr() {
        try {
            String configFilePath = System.getProperty("config.file");

            if ((configFilePath == null) || configFilePath.isEmpty()) {
                throw new IllegalArgumentException(
                    "Config file path is not specified or is empty.");
            }

            Path absolutePath = Paths.get(configFilePath).toAbsolutePath();

            return Props.getProp(absolutePath.toString(), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);

            return null;
        }
    }
}
