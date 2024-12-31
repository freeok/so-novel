package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.convert.ChapterConverter;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.CrawlUtils;
import lombok.SneakyThrows;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class ChapterParser extends Source {

    private static final int TIMEOUT_MILLS = 15_000;
    private final ChapterConverter chapterConverter;

    public ChapterParser(ConfigBean config) {
        super(config);
        this.chapterConverter = new ChapterConverter(config);
    }

    // 用于测试
    public Chapter parse(Chapter chapter) {
        String content = crawl(chapter.getUrl(), 0);
        chapter.setContent(content);
        return chapter;
    }

    public Chapter parse(Chapter chapter, CountDownLatch latch, SearchResult sr) {
        try {
            long interval = CrawlUtils.randomInterval(config, false);
            Console.log("<== 正在下载: 【{}】 间隔 {} ms", chapter.getTitle(), interval);
            // ExceptionUtils.randomThrow();
            chapter.setContent(crawl(chapter.getUrl(), interval));
            latch.countDown();
            if ("zh-Hant".equals(this.rule.getLanguage())) {
                chapter = ChineseConverter.t2s(chapter);
            }
            return chapterConverter.convert(chapter, config.getExtName());

        } catch (Exception e) {
            Chapter retry = retry(chapter, latch, sr);
            if (retry != null && "zh-Hant".equals(this.rule.getLanguage())) {
                retry = ChineseConverter.t2s(retry);
            }
            return retry;
        }
    }

    /**
     * 爬取正文内容
     */
    @SneakyThrows
    private String crawl(String url, long interval) {
        Thread.sleep(interval);
        boolean isPaging = this.rule.getChapter().isPagination();
        Document document;

        // 章节不分页，只请求一次
        if (!isPaging) {
            document = getConn(url, TIMEOUT_MILLS).get();
            Elements elContent = document.select(this.rule.getChapter().getContent());
            return elContent.html();
        }

        String nextUrl = url;
        StringBuilder sb = new StringBuilder();
        // 章节分页
        while (true) {
            document = getConn(nextUrl, TIMEOUT_MILLS).get();
            Elements elContent = document.select(this.rule.getChapter().getContent());
            sb.append(elContent.html());
            Elements elNextPage = document.select(this.rule.getChapter().getNextPage());
            // 章节最后一页 TODO 针对书源2，此处容易出错
            if (elNextPage.text().contains("下一章")) break;
            String href = elNextPage.attr("href");
            nextUrl = CrawlUtils.normalizeUrl(href, this.rule.getUrl());
            Thread.sleep(interval);
        }

        return sb.toString();
    }

    private Chapter retry(Chapter chapter, CountDownLatch latch, SearchResult sr) {
        for (int attempt = 1; attempt <= config.getMaxRetryAttempts(); attempt++) {
            try {
                long interval = CrawlUtils.randomInterval(config, true);
                Console.log("==> 章节下载失败，正在重试: 【{}】，尝试次数: {}/{}，重试间隔：{} ms",
                        chapter.getTitle(), attempt, config.getMaxRetryAttempts(), interval);
                chapter.setContent(crawl(chapter.getUrl(), interval));
                Console.log("<== 重试成功: 【{}】", chapter.getTitle());
                latch.countDown();
                return chapterConverter.convert(chapter, config.getExtName());

            } catch (Exception e) {
                Console.error("==> 第 {} 次重试失败: 【{}】，原因: {}", attempt, chapter.getTitle(), e.getMessage());
                if (attempt == config.getMaxRetryAttempts()) {
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
        String path = StrUtil.format("{}{}《{}》（{}）下载失败章节.log", config.getDownloadPath(), File.separator, sr.getBookName(), sr.getAuthor());

        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            // 自带换行符
            pw.println(line);

        } catch (IOException e) {
            Console.error(e);
        }
    }

}