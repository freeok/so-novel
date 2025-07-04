package com.pcdd.sonovel.util;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.context.BookContext;
import com.pcdd.sonovel.model.Book;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class LogUtils {

    private final Cache<String, File> CACHE_FILE;

    static {
        CACHE_FILE = CacheUtil.newFIFOCache(1);
    }

    public File getLogFile() {
        Book book = BookContext.get();
        String key = StrUtil.format("{} {}", book.getBookName(), book.getAuthor());
        if (CACHE_FILE.get(key) != null) {
            return CACHE_FILE.get(key);
        }

        String fileName = StrUtil.format("{} ({}) 章节下载 {}.log", book.getBookName(), book.getAuthor(), DateUtil.today());
        File logFile = FileUtil.touch("logs", fileName);
        CACHE_FILE.put(key, logFile);

        return logFile;
    }

    public void info(String template, Object... args) {
        String msg = StrUtil.format(template, args);
        String logLine = StrUtil.format("[{}] [INFO] {}\n", DateUtil.now(), msg);
        FileUtil.appendUtf8String(logLine, getLogFile());
    }

    public void warn(String template, Object... args) {
        String msg = StrUtil.format(template, args);
        String logLine = StrUtil.format("[{}] [WARN] {}\n", DateUtil.now(), msg);
        FileUtil.appendUtf8String(logLine, getLogFile());
    }

    public void error(String template, Object... values) {
        String msg = StrUtil.format(template, values);
        String logLine = StrUtil.format("[{}] [ERROR] {}\n", DateUtil.now(), msg);
        FileUtil.appendUtf8String(logLine, getLogFile());
    }

    public void error(Throwable t, String template, Object... values) {
        String msg = StrUtil.format(template, values);
        String logLine = StrUtil.format("[{}] [ERROR] {}\n{}\n",
                DateUtil.now(), msg,
                t == null ? "" : getStackTrace(t));
        FileUtil.appendUtf8String(logLine, getLogFile());
    }

    private String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.toString()).append("\n");
        for (StackTraceElement el : t.getStackTrace()) {
            sb.append("\tat ").append(el).append("\n");
        }
        return sb.toString();
    }

}