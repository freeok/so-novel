package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.convert.ChapterConverter;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.*;
import com.pcdd.sonovel.util.BookContext;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.HttpClientContext;
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static org.jline.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class ChapterParser extends Source {

    private final ChapterConverter chapterConverter;

    public ChapterParser(AppConfig config) {
        super(config);
        this.chapterConverter = new ChapterConverter(config);
    }

    // 用于测试
    @SneakyThrows
    public Chapter parse(Chapter chapter) {
        Rule.Chapter r = this.rule.getChapter();
        Document document;
        OkHttpClient client = HttpClientContext.get();

        try (Response resp = CrawlUtils.request(client, chapter.getUrl(), r.getTimeout())) {
            document = Jsoup.parse(resp.body().string(), r.getBaseUri());
        }

        chapter.setTitle(JsoupUtils.selectAndInvokeJs(document, r.getTitle()));
        String content = fetchContent(chapter.getUrl(), RandomUtil.randomInt(100, 200));
        chapter.setContent(content);

        return chapter;
    }

    public Chapter parse(Chapter chapter, CountDownLatch latch) {
        try {
            long interval = CrawlUtils.randomInterval(config);
            Console.log("<== 正在下载: 【{}】 间隔 {} ms", chapter.getTitle(), interval);

            String content = fetchContent(chapter.getUrl(), interval);
            Assert.notEmpty(content, "正文内容为空");
            chapter.setContent(content);

            // 确保简繁互转最后调用
            return ChineseConverter.convert(chapterConverter.convert(chapter), this.rule.getLanguage(), config.getLanguage());

        } catch (Exception e) {
            Chapter retryChapter = retry(chapter, e.getMessage());
            return retryChapter == null ? null : ChineseConverter.convert(retryChapter, this.rule.getLanguage(), config.getLanguage());
        } finally {
            latch.countDown();
        }
    }

    private Chapter retry(Chapter chapter, String errMsg) {
        for (int attempt = 1; attempt <= config.getMaxRetryAttempts(); attempt++) {
            try {
                long interval = CrawlUtils.randomInterval(config, true);
                Console.log(render("<== : 章节【{}】下载失败，正在重试。重试次数: {}/{} 重试间隔: {} ms 原因: {}", "red"),
                        chapter.getTitle(), attempt, config.getMaxRetryAttempts(), interval, errMsg);

                String content = fetchContent(chapter.getUrl(), interval);
                Assert.notEmpty(content, "正文内容为空");
                chapter.setContent(content);

                Console.log(render("<== 重试成功: 【{}】", "green"), chapter.getTitle());
                return chapterConverter.convert(chapter);

            } catch (Exception e) {
                Console.error(render("<== 第 {} 次重试失败: 【{}】 原因: {}", "red"), attempt, chapter.getTitle(), e.getMessage());
                if (attempt == config.getMaxRetryAttempts()) {
                    // 最终失败时记录日志
                    saveDownloadErrorLog(chapter, e.getMessage());
                }
            }
        }

        return null;
    }

    private void saveDownloadErrorLog(Chapter chapter, String errMsg) {
        Book book = BookContext.get();
        String line = StrUtil.format("下载失败章节: 【{}】({})\t原因: {}", chapter.getTitle(), chapter.getUrl(), errMsg);
        String path = StrUtil.format("{}{}《{}》({}) 下载失败章节.log",
                config.getDownloadPath(), File.separator, book.getBookName(), book.getAuthor());

        try (PrintWriter pw = new PrintWriter(new FileWriter(path, StandardCharsets.UTF_8, true))) {
            pw.println(line);

        } catch (IOException e) {
            Console.error(e);
        }
    }

    /**
     * 爬取正文内容
     *
     * @param url      章节 url
     * @param interval 爬取间隔（毫秒）
     */
    @SneakyThrows
    public String fetchContent(String url, long interval) {
        Rule.Chapter r = rule.getChapter();
        return r.isPagination()
                ? fetchPaginatedContent(url, interval, r)
                : fetchSinglePageContent(url, interval, r);
    }

    @SneakyThrows
    private String fetchSinglePageContent(String url, long interval, Rule.Chapter r) {
        OkHttpClient client = HttpClientContext.get();

        try (Response resp = CrawlUtils.request(client, url, r.getTimeout())) {
            Document doc = Jsoup.parse(resp.body().string(), r.getBaseUri());

            // 删除每个元素的所有属性，防止标签和属性间的空格被后续清理，导致标签错误
            Elements contentEl = JsoupUtils.select(doc, r.getContent());
            for (Element el : contentEl.select("*")) {
                el.clearAttributes();
            }

            Thread.sleep(interval);

            return JsoupUtils.invokeJs(r.getContent(), contentEl.html());
        }
    }

    @SneakyThrows
    private String fetchPaginatedContent(String startUrl, long interval, Rule.Chapter r) {
        String nextUrl = startUrl;
        StringBuilder contentBuilder = new StringBuilder();
        OkHttpClient client = HttpClientContext.get();

        while (true) {
            Document doc;
            try (Response resp = CrawlUtils.request(client, nextUrl, r.getTimeout())) {
                doc = Jsoup.parse(resp.body().string(), r.getBaseUri());
            }
            contentBuilder.append(JsoupUtils.selectAndInvokeJs(doc, r.getContent(), ContentType.HTML));
            // 获取下一页按钮元素
            Elements nextEls = JsoupUtils.select(doc, r.getNextPage());
            String candidateNext = resolveNextUrl(doc, nextEls, r);
            if (isLastPage(candidateNext, nextEls, r)) {
                break;
            }

            nextUrl = candidateNext;
            Thread.sleep(interval);
        }

        return contentBuilder.toString();
    }

    private String resolveNextUrl(Document doc, Elements nextEls, Rule.Chapter r) {
        // 从 JS 获取下一页链接
        if (r.getNextPageInJs() != null) {
            return JsoupUtils.selectAndInvokeJs(doc, r.getNextPageInJs(), ContentType.HTML);
        }
        // FIXME nextEls NPE https://github.com/freeok/so-novel/issues/148#issuecomment-2826226097
        if (nextEls.isEmpty()) {
            Console.error("分页章节正文获取为空，可能被限流！\n出错链接：{}\n链接内容：{}", doc.baseUri(), doc.body().text());
            return null;
        }
        // 从按钮获取下一页链接
        return nextEls.first().absUrl("href");
    }

    private boolean isLastPage(String nextUrl, Elements nextEls, Rule.Chapter r) {
        if (nextUrl == null) {
            return true;
        }

        // 正则判断是否为章节最后一页
        boolean endByChapterRule = r.getNextChapterLink() != null && nextUrl.matches(r.getNextChapterLink());
        // 通用规则，大多数分页的 url 以 "_个位数字.html" 结尾。&& 部分网站会用“下一章”代替“下一页”
        boolean genericEnd = !nextUrl.matches(".*[-_]\\d\\.html") && nextEls.text().matches(".*(下一章|没有了|>>|书末页).*");

        return endByChapterRule || genericEnd;
    }

}