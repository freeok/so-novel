package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Assert;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.convert.ChapterConverter;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.ContentType;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.JsoupUtils;
import com.pcdd.sonovel.util.LogUtils;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.concurrent.CountDownLatch;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class ChapterParser extends Source {

    private final OkHttpClient httpClient = HttpClientContext.get();
    private final ChapterConverter chapterConverter;

    public ChapterParser(AppConfig config) {
        super(config);
        this.chapterConverter = new ChapterConverter(config);
    }

    // 用于测试
    @SneakyThrows
    public Chapter parse(Chapter chapter) {
        Rule.Chapter r = this.rule.getChapter();

        // 获取章节名
        try (Response resp = CrawlUtils.request(httpClient, chapter.getUrl(), r.getTimeout())) {
            Document document = Jsoup.parse(resp.body().string(), r.getBaseUri());
            chapter.setTitle(JsoupUtils.selectAndInvokeJs(document, r.getTitle()));
        }

        chapter.setContent(fetchContent(chapter.getUrl(), CrawlUtils.randomInterval(config)));

        return ChineseConverter.convert(chapterConverter.convert(chapter), this.rule.getLanguage(), config.getLanguage());
    }

    public Chapter parse(Chapter chapter, CountDownLatch latch) {
        try {
            long interval = CrawlUtils.randomInterval(config);
            LogUtils.info("正在下载: 【{}】 间隔 {} ms", chapter.getTitle(), interval);

            String content = fetchContent(chapter.getUrl(), interval);
            Assert.notEmpty(content, "正文内容为空");
            chapter.setContent(content);

            // 确保简繁互转最后调用
            return ChineseConverter.convert(chapterConverter.convert(chapter), this.rule.getLanguage(), config.getLanguage());

        } catch (Exception e) {
            Chapter retryChapter = retry(chapter, e);
            return retryChapter == null ? null : ChineseConverter.convert(retryChapter, this.rule.getLanguage(), config.getLanguage());
        } finally {
            latch.countDown();
        }
    }

    private Chapter retry(Chapter chapter, Exception ex) {
        for (int attempt = 1; attempt <= config.getMaxRetries(); attempt++) {
            try {
                long interval = CrawlUtils.randomInterval(config, true);
                LogUtils.warn("【{}】下载失败，正在重试。重试次数: {}/{} 重试间隔: {} ms 原因: {}",
                        chapter.getTitle(), attempt, config.getMaxRetries(), interval, ex.getMessage());

                String content = fetchContent(chapter.getUrl(), interval);
                Assert.notEmpty(content, "正文内容为空");
                chapter.setContent(content);

                LogUtils.info("重试成功: 【{}】", chapter.getTitle());
                return chapterConverter.convert(chapter);

            } catch (Exception e) {
                LogUtils.warn("第 {} 次重试失败: 【{}】 原因: {}", attempt, chapter.getTitle(), e.getMessage());
                // 最终失败时记录日志
                if (attempt == config.getMaxRetries()) {
                    LogUtils.error(e, "下载失败章节: 【{}】({})\t原因: {}", chapter.getTitle(), chapter.getUrl(), e.getMessage());
                }
            }
        }

        return null;
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
        // 获取下一章的间隔
        Thread.sleep(interval);
        return r.isPagination()
                ? fetchPaginatedContent(url, interval, r)
                : fetchSinglePageContent(url, r);
    }

    @SneakyThrows
    private String fetchSinglePageContent(String url, Rule.Chapter r) {
        Document doc;

        try (Response resp = CrawlUtils.request(httpClient, url, r.getTimeout())) {
            doc = Jsoup.parse(resp.body().string(), r.getBaseUri());
        }

        return JsoupUtils.selectAndInvokeJs(doc, r.getContent(), ContentType.HTML);
    }

    @SneakyThrows
    private String fetchPaginatedContent(String startUrl, long interval, Rule.Chapter r) {
        String nextUrl = startUrl;
        StringBuilder contentBuilder = new StringBuilder();

        while (true) {
            Document doc;
            try (Response resp = CrawlUtils.request(httpClient, nextUrl, r.getTimeout())) {
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
            // 获取下一分页章节的间隔
            Thread.sleep(interval);
        }

        return contentBuilder.toString();
    }

    private String resolveNextUrl(Document doc, Elements nextEls, Rule.Chapter r) {
        // 从 JS 获取下一页链接
        if (r.getNextPageInJs() != null) {
            return JsoupUtils.selectAndInvokeJs(doc, r.getNextPageInJs(), ContentType.HTML);
        }
        if (nextEls.isEmpty()) {
            LogUtils.error("分页章节正文获取为空，可能被限流！出错链接：{} 链接内容：{}", doc.baseUri(), doc.body().text());
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