package com.pcdd.sonovel.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
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

    /**
     * 获取远程文件大小，HEAD 请求不会下载文件内容，只会返回文件的元数据（例如文件大小）
     *
     * @return 字节
     */
    @SneakyThrows
    public long fileSize(String fileUrl) {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 使用 HEAD 请求方法，只获取头部信息
        conn.setRequestMethod("HEAD");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);
        int contentLength = conn.getContentLength();
        conn.disconnect();

        return contentLength == -1 ? 0 : contentLength;
    }

}