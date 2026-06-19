package com.pcdd.sonovel.util;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author pcdd
 * Created at 2024/12/4
 */
@UtilityClass
public class FileUtils {

    /**
     * 文件排序，按文件名中下划线前的数字升序
     */
    public List<File> sortFilesByName(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return Collections.emptyList();

        return Arrays.stream(files)
                .sorted((o1, o2) -> {
                    int no1 = extractOrder(o1.getName());
                    int no2 = extractOrder(o2.getName());
                    return Integer.compare(no1, no2);
                }).toList();
    }

    private int extractOrder(String name) {
        try {
            String prefix = StrUtil.subBefore(name, "_", false);
            return Integer.parseInt(prefix);
        } catch (Exception e) { // Integer.parseInt 失败时返回 Integer.MAX_VALUE，将无法解析的文件名排到末尾
            return Integer.MAX_VALUE;
        }
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
                    .replace('*', '＊')
                    .replace('?', '？')
                    .replace('"', '\'')
                    .replace('<', '＜')
                    .replace('>', '＞')
                    .replaceAll("[/\\\\|]", "_");
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
            return fileName
                    .replace('.', '。')
                    .replace(':', '：')
                    .replace('/', '／')
                    .replace('\000', '_');
        } else { // others
            return fileName.replace("/", "");
        }

    }

    /**
     * 如果是绝对路径，则直接返回；如果是相对路径，则基于 user.dir 拼接并返回完整路径。
     *
     * @param path 相对路径或绝对路径
     * @return 解析后的路径字符串
     */
    public String toAbsolutePath(String path) {
        return Paths.get(path).toAbsolutePath().toString();
    }

}