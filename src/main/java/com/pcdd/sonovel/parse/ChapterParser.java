package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.convert.ChapterConverter;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.*;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.SneakyThrows;
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
        Document document = jsoup(chapter.getUrl())
                .timeout(this.rule.getChapter().getTimeout())
                .get();
        chapter.setTitle(JsoupUtils.selectAndInvokeJs(document, this.rule.getChapter().getTitle()));
        chapter.setContent(crawl(chapter.getUrl(), RandomUtil.randomInt(100, 200)));
        return chapter;
    }

    public Chapter parse(Chapter chapter, CountDownLatch latch, SearchResult sr) {
        try {
            long interval = CrawlUtils.randomInterval(config);
            Console.log("<== 正在下载: 【{}】 间隔 {} ms", chapter.getTitle(), interval);
            // ExceptionUtils.randomThrow();
            chapter.setContent(crawl(chapter.getUrl(), interval));
            // 确保简繁互转最后调用
            return ChineseConverter.convert(chapterConverter.convert(chapter), this.rule.getLanguage(), config.getLanguage());

        } catch (Exception e) {
            Chapter retryChapter = retry(chapter, sr);
            return retryChapter == null ? null : ChineseConverter.convert(retryChapter, this.rule.getLanguage(), config.getLanguage());
        } finally {
            latch.countDown();
        }
    }

    private Chapter retry(Chapter chapter, SearchResult sr) {
        for (int attempt = 1; attempt <= config.getMaxRetryAttempts(); attempt++) {
            try {
                long interval = CrawlUtils.randomInterval(config, true);
                Console.log(render("<== 章节下载失败，正在重试: 【{}】 重试次数: {}/{} 重试间隔: {} ms", "red"),
                        chapter.getTitle(), attempt, config.getMaxRetryAttempts(), interval);
                chapter.setContent(crawl(chapter.getUrl(), interval));
                Console.log(render("<== 重试成功: 【{}】", "green"), chapter.getTitle());
                return chapterConverter.convert(chapter);

            } catch (Exception e) {
                Console.error(render("<== 第 {} 次重试失败: 【{}】 原因: {}", "red"), attempt, chapter.getTitle(), e.getMessage());
                if (attempt == config.getMaxRetryAttempts()) {
                    // 最终失败时记录日志
                    saveDownloadErrorLog(chapter, sr, e.getMessage());
                }
            }
        }

        return null;
    }

    /**
     * 爬取正文内容
     *
     * @param url      章节 url
     * @param interval 爬取间隔
     */
    @SneakyThrows
    private String crawl(String url, long interval) {
        Rule.Chapter ruleChapter = this.rule.getChapter();
        Document document;
        Thread.sleep(interval);
        // 章节不分页，只请求一次
        if (!ruleChapter.isPagination()) {
            document = jsoup(url)
                    .timeout(ruleChapter.getTimeout())
                    .get();
            Elements contentEl = JsoupUtils.select(document, ruleChapter.getContent());

            // 删除每个元素的所有属性，防止标签和属性间的空格被后续清理，导致标签错误
            for (Element el : contentEl.select("*")) {
                el.clearAttributes();
            }

            return JsoupUtils.invokeJs(ruleChapter.getContent(), contentEl.html());
        }

        String nextUrl = url;
        StringBuilder contentBuilder = new StringBuilder();
        // 章节分页
        for (int i = 0; ; i++) {
            // 第一次执行无需对 nextUrl 进行判断
            String currentUrl = i == 0 ? nextUrl : JsoupUtils.invokeJs(ruleChapter.getNextPage(), nextUrl);
            if (StrUtil.isEmpty(currentUrl)) break;
            document = jsoup(currentUrl)
                    .timeout(ruleChapter.getTimeout())
                    .get();
            contentBuilder.append(JsoupUtils.selectAndInvokeJs(document, ruleChapter.getContent(), ContentType.HTML));

            // 获取下一页按钮元素
            Elements nextEls = JsoupUtils.select(document, ruleChapter.getNextPage());
            // 判断是否为章节最后一页，依据：不以 "_个位数字.html" 结尾
            if (!nextEls.attr("href").matches(".*_\\d\\.html/?$")) break;
            // 这种方法不可靠，因为部分网站会用“下一章”代替“下一页”
            // if (nextEls.text().matches(".*(下一章|没有了|>>|书末页).*")) break;

            // 从 JS 获取下一页链接
            if (ruleChapter.getNextPageInJs() != null) {
                nextUrl = JsoupUtils.selectAndInvokeJs(document, ruleChapter.getNextPageInJs(), ContentType.HTML);
            } else { // 从按钮获取下一页链接
                // FIXME nextEls NPE https://github.com/freeok/so-novel/issues/148#issuecomment-2826226097
                if (StrUtil.isNotEmpty(nextEls.toString())) {
                    nextUrl = nextEls.first().absUrl("href");
                } else {
                    Console.error("nextUrl = {}", nextUrl);
                }
            }
            Thread.sleep(interval);
        }

        return contentBuilder.toString();
    }

    private void saveDownloadErrorLog(Chapter chapter, SearchResult sr, String errMsg) {
        String line = StrUtil.format("下载失败章节：【{}】({}) 原因：{}", chapter.getTitle(), chapter.getUrl(), errMsg);
        String path = StrUtil.format("{}{}《{}》({})下载失败章节.log", config.getDownloadPath(), File.separator, sr.getBookName(), sr.getAuthor());

        try (PrintWriter pw = new PrintWriter(new FileWriter(path, StandardCharsets.UTF_8, true))) {
            pw.println(line);

        } catch (IOException e) {
            Console.error(e);
        }
    }

}