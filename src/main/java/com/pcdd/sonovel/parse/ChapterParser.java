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
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.SneakyThrows;
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
        Document document;

        try (Response resp = request(chapter.getUrl())) {
            document = Jsoup.parse(resp.body().string(), this.rule.getChapter().getBaseUri());
        }

        chapter.setTitle(JsoupUtils.selectAndInvokeJs(document, this.rule.getChapter().getTitle()));
        chapter.setContent(fetchContent(chapter.getUrl(), RandomUtil.randomInt(100, 200)));

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
     * @param interval 爬取间隔
     */
    @SneakyThrows
    private String fetchContent(String url, long interval) {
        Document document;
        Rule.Chapter r = this.rule.getChapter();

        // 章节不分页，只请求一次
        if (!r.isPagination()) {
            try (Response resp = request(url)) {
                document = Jsoup.parse(resp.body().string(), r.getBaseUri());
            }

            Elements contentEl = JsoupUtils.select(document, r.getContent());
            // 删除每个元素的所有属性，防止标签和属性间的空格被后续清理，导致标签错误
            for (Element el : contentEl.select("*")) {
                el.clearAttributes();
            }

            Thread.sleep(interval);

            return JsoupUtils.invokeJs(r.getContent(), contentEl.html());
        }

        String nextUrl = url;
        StringBuilder contentBuilder = new StringBuilder();
        // 章节分页
        while (true) {
            try (Response resp = request(nextUrl)) {
                document = Jsoup.parse(resp.body().string(), r.getBaseUri());
            }
            contentBuilder.append(JsoupUtils.selectAndInvokeJs(document, r.getContent(), ContentType.HTML));

            // 获取下一页按钮元素
            Elements nextEls = JsoupUtils.select(document, r.getNextPage());

            // 从 JS 获取下一页链接
            if (r.getNextPageInJs() != null) {
                nextUrl = JsoupUtils.selectAndInvokeJs(document, r.getNextPageInJs(), ContentType.HTML);
            } else { // 从按钮获取下一页链接
                // FIXME nextEls NPE https://github.com/freeok/so-novel/issues/148#issuecomment-2826226097
                if (StrUtil.isNotEmpty(nextEls.toString())) {
                    Element first = nextEls.first();
                    nextUrl = first.absUrl("href");
                } else {
                    Console.error("分页章节正文获取为空，可能被限流！出错链接：{}", nextUrl);
                    break;
                }
            }

            // 正则判断是否为章节最后一页
            boolean b1 = r.getNextChapterLink() != null && nextUrl.matches(r.getNextChapterLink());
            // 通用规则，大多数分页的 url 以 "_个位数字.html" 结尾。&& 部分网站会用“下一章”代替“下一页”
            boolean b2 = !nextUrl.matches(".*[-_]\\d\\.html") && nextEls.text().matches(".*(下一章|没有了|>>|书末页).*");
            if (b1 || b2) break;

            Thread.sleep(interval);
        }

        return contentBuilder.toString();
    }

}