package com.pcdd.sonovel.utils;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.context.BookContext;
import com.pcdd.sonovel.model.LogLevel;
import com.pcdd.sonovel.model.Rule.Book;
import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * 注意，成员方法必须在 BookContext.set() 后调用
 */
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

    private void write(LogLevel level, String template, Object... args) {
        String msg = StrUtil.format(template, args);
        String logLine = StrUtil.format("[{}] [{}] {}\n", DateUtil.now(), level, msg);
        FileUtil.appendUtf8String(logLine, getLogFile());
    }

    /**
     * enableProgressbar 决定日志输出方式
     */
    public void logByProgressbar(LogLevel level, int enableProgressbar, String template, Object... args) {
        switch (level) {
            case INFO -> {
                if (enableProgressbar == 1) {
                    info(template, args);
                } else {
                    infoConsole(template, args);
                }
            }
            case WARN -> {
                if (enableProgressbar == 1) {
                    warn(template, args);
                } else {
                    warnConsole(template, args);
                }
            }
            case ERROR -> {
                if (enableProgressbar == 1) {
                    error(template, args);
                } else {
                    errorConsole(template, args);
                }
            }
        }
    }

    /**
     * 写入 INFO 级别日志文件
     */
    public void info(String template, Object... args) {
        write(LogLevel.INFO, template, args);
    }

    /**
     * 写入 INFO 级别日志文件，并输出到控制台
     */
    public void infoConsole(String template, Object... args) {
        Console.log("[INFO] " + template, args);
        info(template, args);
    }

    /**
     * 写入 WARN 级别日志文件
     */
    public void warn(String template, Object... args) {
        write(LogLevel.WARN, template, args);
    }

    /**
     * 写入 WARN 级别日志文件，并输出到控制台
     */
    public void warnConsole(String template, Object... args) {
        Console.log("[WARN] " + template, args);
        warn(template, args);
    }

    /**
     * 写入 ERROR 级别日志文件
     */
    public void error(String template, Object... args) {
        write(LogLevel.ERROR, template, args);
    }

    /**
     * 写入 ERROR 级别日志文件，并输出到控制台
     */
    public void errorConsole(String template, Object... args) {
        Console.error("[ERROR] " + template, args);
        error(template, args);
    }

    /**
     * 写入 ERROR 级别日志文件
     */
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