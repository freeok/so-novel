package com.pcdd.sonovel.util;

import com.pcdd.sonovel.core.ConfigManager;

public class ConfigConsts {

    public static final ConfigManager cm = ConfigManager.getInstance();

    public static final String VERSION = cm.getSys().getStr("version");
    public static final int SOURCE_ID = cm.getSys().getInt("source_id");

    public static final String SAVE_PATH = cm.getUsr().getStr("savePath");
    public static final String EXT_NAME = cm.getUsr().getStr("extName");
    public static final int MIN_INTERVAL = cm.getUsr().getInt("min");
    public static final int MAX_INTERVAL = cm.getUsr().getInt("max");
    public static final int THREADS = cm.getUsr().getInt("threads");
    public static final int MAX_RETRY_ATTEMPTS = cm.getUsr().getInt("retryCount");
    public static final int RETRY_MIN_INTERVAL = cm.getUsr().getInt("retryMin");
    public static final int RETRY_MAX_INTERVAL = cm.getUsr().getInt("retryMax");

    private ConfigConsts() {
    }

}
