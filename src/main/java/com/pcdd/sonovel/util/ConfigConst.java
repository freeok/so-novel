package com.pcdd.sonovel.util;

import com.pcdd.sonovel.core.ConfigManager;

/**
 * @author pcdd
 */
public class ConfigConst {

    public static final ConfigManager cm = ConfigManager.getInstance();

    public static final String VERSION = cm.getSys().getStr("version");

    public static final int SOURCE_ID = cm.getUsr().getInt("source_id", 1);
    public static final String SAVE_PATH = cm.getUsr().getStr("savePath", "downloads");
    public static final String EXT_NAME = cm.getUsr().getStr("extName", "epub");
    public static final int THREADS = cm.getUsr().getInt("threads", -1);
    public static final int MIN_INTERVAL = cm.getUsr().getInt("min", 10);
    public static final int MAX_INTERVAL = cm.getUsr().getInt("max", 100);
    public static final int MAX_RETRY_ATTEMPTS = cm.getUsr().getInt("retryCount", 3);
    public static final int RETRY_MIN_INTERVAL = cm.getUsr().getInt("retryMin", 300);
    public static final int RETRY_MAX_INTERVAL = cm.getUsr().getInt("retryMax", 3000);

    private ConfigConst() {
    }

}
