package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.core.ChapterConverter;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.Settings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author pcdd
 */
public class ChapterParser extends Parser {

    private static final String EXT_NAME;
    private static final String SAVE_PATH;
    private static final long MIN_TIME_INTERVAL;
    private static final long MAX_TIME_INTERVAL;
    // 下载失败章节最大重试次数
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int TIMEOUT_MILLS = 15_000;

    static {
        Props usr = Settings.usr();
        EXT_NAME = usr.getStr("extName");
        SAVE_PATH = usr.getStr("savePath");
        MIN_TIME_INTERVAL = usr.getLong("min");
        MAX_TIME_INTERVAL = usr.getLong("max");
    }

    public ChapterParser(int sourceId) {
        super(sourceId);
    }

    public Chapter parse(Chapter chapter, SearchResult sr, CountDownLatch latch) {
        try {
            // 设置时间间隔
            long timeInterval = ThreadLocalRandom.current().nextLong(MIN_TIME_INTERVAL, MAX_TIME_INTERVAL);
            TimeUnit.MILLISECONDS.sleep(timeInterval);
            Console.log("<== 正在下载: 【{}】 间隔 {} ms", chapter.getTitle(), timeInterval);
            Document document = Jsoup.parse(URLUtil.url(chapter.getUrl()), TIMEOUT_MILLS);
            // 小说正文 html 格式
            chapter.setContent(document.select(this.rule.getChapter().getContent()).html());
            latch.countDown();
            return ChapterConverter.convert(chapter, EXT_NAME);

        } catch (Exception e) {
            return retry(chapter, latch, sr);
        }
    }

    private Chapter retry(Chapter chapter, CountDownLatch latch, SearchResult sr) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                Console.log("==> 正在重试下载失败章节: 【{}】，尝试次数: {}/{}", chapter.getTitle(), attempt, MAX_RETRY_ATTEMPTS);

                // 随机时间间隔以避免被反爬机制识别
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(100, 3000));

                // 再次尝试下载
                Document document = Jsoup.parse(URLUtil.url(chapter.getUrl()), TIMEOUT_MILLS);
                chapter.setContent(document.select(this.rule.getChapter().getContent()).html());
                Console.log("<== 重试成功: 【{}】", chapter.getTitle());
                latch.countDown();
                return ChapterConverter.convert(chapter, EXT_NAME);

            } catch (Exception e) {
                Console.error("==> 重试失败: 【{}】，原因: {}", chapter.getTitle(), e.getMessage());
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    latch.countDown();
                    // 最终失败时记录日志
                    saveErrorLog(chapter, sr, e.getMessage());
                }
            }
        }

        return null;
    }

    private void saveErrorLog(Chapter chapter, SearchResult sr, String errMsg) {
        String line = StrUtil.format("下载失败章节：【{}】({})，原因：{}", chapter.getTitle(), chapter.getUrl(), errMsg);
        String path = StrUtil.format("{}{}《{}》（{}）下载失败章节.log", SAVE_PATH, File.separator, sr.getBookName(), sr.getAuthor());

        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            // 自带换行符
            pw.println(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
