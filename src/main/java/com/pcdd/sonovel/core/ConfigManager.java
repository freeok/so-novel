package com.pcdd.sonovel.core;

import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.util.Settings;

/**
 * @author pcdd
 */
public class ConfigManager {

    private final Props sysProps;
    private final Props usrProps;

    private ConfigManager() {
        sysProps = Settings.sys();
        usrProps = Settings.usr();
    }

    // 提供全局访问点
    public static ConfigManager getInstance() {
        return Holder.INSTANCE;
    }

    // 静态内部类，负责持有单例实例
    private static class Holder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    public Props getSys() {
        return this.sysProps;
    }

    public Props getUsr() {
        return this.usrProps;
    }

}
