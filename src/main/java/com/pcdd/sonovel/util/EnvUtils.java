package com.pcdd.sonovel.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EnvUtils {

    private final String ENV = System.getProperty("env", "prod").toLowerCase();

    public boolean isDev() {
        return "dev".equals(ENV);
    }

    public boolean isProd() {
        return "prod".equals(ENV);
    }

    public String current() {
        return ENV;
    }

}