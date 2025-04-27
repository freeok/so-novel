package com.pcdd.sonovel.util;

import cn.hutool.core.util.StrUtil;
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
                    int no1 = Integer.parseInt(StrUtil.subBefore(o1.getName(), "_", false));
                    int no2 = Integer.parseInt(StrUtil.subBefore(o2.getName(), "_", false));
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

    /**
     * 替换文件名非法字符，仅用于文件名而非路径
     */
    public String sanitizeFileName(String fileName) {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return fileName
                    .replace(':', '：')
                    .replace('*', '※')
                    .replace('?', '？')
                    .replace('"', '\'')
                    .replace('<', '《')
                    .replace('>', '》')
                    .replaceAll("[/\\\\|]", "_");
        } else if (osName.contains("mac")) {
            return fileName
                    .replace('.', '。')
                    .replace(':', '：');
        } else { // linux & others
            return fileName.replace("/", "");
        }

    }

}