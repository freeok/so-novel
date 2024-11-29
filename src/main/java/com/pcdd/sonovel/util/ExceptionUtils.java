package com.pcdd.sonovel.util;

import lombok.experimental.UtilityClass;

/**
 * @author pcdd
 * Created at 2024/11/22
 */
@UtilityClass
public class ExceptionUtils {

    // 随机抛异常，测试用
    public void randomThrow() {
        int i = System.currentTimeMillis() % 2 == 0 ? 0 : 1 / 0;
    }

}
