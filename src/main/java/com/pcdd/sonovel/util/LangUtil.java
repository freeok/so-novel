package com.pcdd.sonovel.util;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class LangUtil {

    /**
     * 获取当前系统语言
     */
    public String getCurrentLang() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage(); // zh
        String country = locale.getCountry();   // CN / TW
        String script = locale.getScript();     // Hant / Hans / 空

        // zh_CN
        if ("zh".equals(language) && "CN".equals(country)) {
            return LangType.ZH_CN;
        }
        // zh_TW
        if ("zh".equals(language) && "TW".equals(country)) {
            return LangType.ZH_TW;
        }
        // zh_Hant
        if ("zh".equals(language) && "Hant".equalsIgnoreCase(script)) {
            return LangType.ZH_HANT;
        }

        return LangType.ZH_CN;
    }

    /**
     * 是否简体中文
     */
    public boolean isSC() {
        return LangType.ZH_CN.equals(getCurrentLang());
    }

    /**
     * 是否繁體中文
     */
    public boolean isTC() {
        return StrUtil.equalsAny(getCurrentLang(), LangType.ZH_TW, LangType.ZH_HANT);
    }

}