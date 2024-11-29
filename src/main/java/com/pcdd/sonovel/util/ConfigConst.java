package com.pcdd.sonovel.util;

import cn.hutool.setting.Setting;
import cn.hutool.setting.dialect.Props;

/**
 * @author pcdd
 */
public class ConfigConst {

    public static final Props sys = Settings.sys();
    public static final Setting usr = Settings.usr();

    public static final String VERSION = sys.getStr("version");

    public static final int SOURCE_ID = usr.getInt("source-id", "base", 1);
    public static final String SAVE_PATH = usr.getStr("save-path", "base", "downloads");
    public static final String EXT_NAME = usr.getStr("extname", "base", "epub");

    public static final int THREADS = usr.getInt("threads", "crawl", -1);
    public static final int MIN_INTERVAL = usr.getInt("min", "crawl", 50);
    public static final int MAX_INTERVAL = usr.getInt("max", "crawl", 100);

    public static final int MAX_RETRY_ATTEMPTS = usr.getInt("max-attempts", "retry", 3);
    public static final int RETRY_MIN_INTERVAL = usr.getInt("min", "retry", 500);
    public static final int RETRY_MAX_INTERVAL = usr.getInt("max", "retry", 2000);

    private ConfigConst() {
    }

}
