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

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 */
public class ChapterParser extends Parser {

    private static final String EXT_NAME;
    private static final String SAVE_PATH;
    private static final long MIN_TIME_INTERVAL;
    private static final long MAX_TIME_INTERVAL;

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
            // 随机抛异常，测试用
            // int i = System.currentTimeMillis() % 2 == 0 ? 0 : 1 / 0;

            // 设置时间间隔
            long timeInterval = ThreadLocalRandom.current().nextLong(MIN_TIME_INTERVAL, MAX_TIME_INTERVAL);
            TimeUnit.MILLISECONDS.sleep(timeInterval);
            Console.log("<== 正在下载: 【{}】 间隔 {} ms", chapter.getTitle(), timeInterval);
            Document document = Jsoup.parse(URLUtil.url(chapter.getUrl()), 15_000);
            // 小说正文 html 格式
            chapter.setContent(document.select(this.rule.getChapter().getContent()).html());
            return ChapterConverter.convert(chapter, EXT_NAME);

        } catch (Exception e) {
            latch.countDown();
            Console.error(render("==> @|red 章节下载失败：【{}】({})，原因：{}|@"), chapter.getTitle(), chapter.getUrl(), e.getMessage());
            saveErrorLog(chapter, sr, e.getMessage());
            // TODO retry
        }

        return null;
    }

    private void saveErrorLog(Chapter chapter, SearchResult sr, String errMsg) {
        String line = StrUtil.format("下载失败章节：【{}】({})，原因：{}", chapter.getTitle(), chapter.getUrl(), errMsg);
        String path = StrUtil.format("{}{}《{}》（{}）下载失败.log", SAVE_PATH, File.separator, sr.getBookName(), sr.getAuthor());

        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            // 自带换行符
            pw.println(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
