package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.pcdd.sonovel.core.ChapterConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.CrawlUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

import static com.pcdd.sonovel.util.ConfigConsts.*;

/**
 * @author pcdd
 */
public class ChapterParser extends Source {

    private static final int TIMEOUT_MILLS = 15_000;

    public ChapterParser(int sourceId) {
        super(sourceId);
    }

    public Chapter parse(Chapter chapter, CountDownLatch latch, SearchResult sr) {
        try {
            Console.log("<== 正在下载: 【{}】", chapter.getTitle());
            chapter.setContent(crawl(chapter.getUrl(), false));
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
                chapter.setContent(crawl(chapter.getUrl(), true));
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

    /**
     * 爬取正文内容
     */
    private String crawl(String url, boolean isRetry) throws InterruptedException, IOException {
        boolean isPaging = this.rule.getChapter().getPagination();
        String nextUrl = url;
        StringBuilder sb = new StringBuilder();

        do {
            Document document = Jsoup.parse(URLUtil.url(nextUrl), TIMEOUT_MILLS);
            String contentType = this.rule.getChapter().getContentType();
            Elements elContent = document.select(this.rule.getChapter().getContent());
            String content = null;

            if ("html".equals(contentType)) content = elContent.html();
            if ("text".equals(contentType)) content = elContent.text();
            sb.append(content);

            // 章节不分页，只请求一次
            if (!isPaging) break;

            Elements elNextPage = document.select(this.rule.getChapter().getNextPage());
            // 章节最后一页 TODO 此处容易出错，先标记
            if (elNextPage.text().contains("下一章")) break;

            String href = elNextPage.attr("href");
            nextUrl = CrawlUtils.normalizeUrl(href, this.rule.getUrl());
            // 随机爬取间隔，建议重试间隔稍微长一点
            CrawlUtils.randomSleep(isRetry ? RETRY_MIN_INTERVAL : MIN_INTERVAL, isRetry ? RETRY_MAX_INTERVAL : MAX_INTERVAL);
        } while (true);

        return sb.toString();
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
