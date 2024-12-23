package com.pcdd.sonovel.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
@UtilityClass
public class FileUtils {

    // 文件排序，按文件名升序
    public List<File> sortFilesByName(File dir) {
        return Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .sorted((o1, o2) -> {
                    String s1 = o1.getName();
                    String s2 = o2.getName();
                    int no1 = Integer.parseInt(s1.substring(0, s1.indexOf("_")));
                    int no2 = Integer.parseInt(s2.substring(0, s2.indexOf("_")));
                    return no1 - no2;
                }).toList();
    }

}
