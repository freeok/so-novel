package com.pcdd.sonovel.action;

import lombok.experimental.UtilityClass;

/**
 * @author pcdd
 * Created at 2024/12/3
 */
@UtilityClass
public class VersionComparator {

    /**
     * 判断 v1 是否小于 v2
     */
    public boolean isSmaller(String v1, String v2) {
        String[] parts1 = normalizeVersion(v1).split("\\.");
        String[] parts2 = normalizeVersion(v2).split("\\.");

        int len = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < len; i++) {
            String p1 = i < parts1.length ? parts1[i] : "0";
            String p2 = i < parts2.length ? parts2[i] : "0";

            if (isNumeric(p1) && isNumeric(p2)) {
                int num1 = Integer.parseInt(p1);
                int num2 = Integer.parseInt(p2);
                if (num1 != num2) {
                    return num1 < num2;
                }
            } else if (isPreRelease(p1) || isPreRelease(p2)) {
                // p1 是 release，所以它更大
                if (!isPreRelease(p1)) return false;
                // p2 是 release，所以它更大
                if (!isPreRelease(p2)) return true;
                int cmp = p1.compareTo(p2);
                if (cmp != 0) {
                    return cmp < 0;
                }
            } else {
                int cmp = p1.compareTo(p2);
                if (cmp != 0) {
                    return cmp < 0;
                }
            }
        }
        // 版本相等
        return false;
    }

    private String normalizeVersion(String version) {
        return version.replaceFirst("^v", "").replace("-", ".");
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    private boolean isPreRelease(String str) {
        // 检测 “beta”、“alpha” 等字符串
        return str.matches("[a-zA-Z]+.*");
    }

}