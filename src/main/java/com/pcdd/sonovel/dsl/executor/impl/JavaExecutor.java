package com.pcdd.sonovel.dsl.executor.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ReUtil;
import com.pcdd.sonovel.dsl.executor.StepExecutor;

import java.util.stream.Collectors;

/**
 * @author pcdd
 * Created at 2026/7/1
 */
public class JavaExecutor implements StepExecutor {

    @Override
    public String execute(String input, String code) {
        if (code.equals("base64.decode()")) {
            // 按换行切分 -> 过滤空行 -> 分别解码 -> 组合成最终字符串
            return input.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(Base64::decodeStr)
                    .collect(Collectors.joining());
        }

        String regex = "string\\.replace\\('([^']*)','([^']*)'\\)";
        if (code.matches(regex)) {
            String s1 = ReUtil.get(regex, code, 1);
            String s2 = ReUtil.get(regex, code, 2);
            return input.replaceAll(s1, s2);
        }

        return input;
    }

}