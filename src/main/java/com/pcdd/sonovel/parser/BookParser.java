package com.pcdd.sonovel.parser;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.HtmlExtractor;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule.Book;
import com.pcdd.sonovel.utils.ChineseConverter;
import com.pcdd.sonovel.utils.CoverUpdater;
import com.pcdd.sonovel.utils.CrawlUtils;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
public class BookParser extends Source {

    private final OkHttpClient httpClient = HttpClientContext.get();

    public BookParser(AppConfig config) {
        super(config);
    }

    @SneakyThrows
    public Book parse(String url) {
        Book r = this.rule.getBook();
        Document document = fetchDocument(url, r.getTimeout(), r.getBaseUri());
        document = handleCloudflareBypass(document, url);

        String bookName = HtmlExtractor.extract(document, r.getBookName());
        String author = HtmlExtractor.extract(document, r.getAuthor());
        Assert.isTrue(StrUtil.isAllNotEmpty(bookName, author), "详情页书名或作者不能为空！DOM:\n{}\n", document);
        String defaultCoverUrl = HtmlExtractor.extract(document, r.getCoverUrl());

        // 部分书源的 meta 格式不标准，需自定义 meta 书源规则
        Book book = new Book();
        book.setUrl(url);
        book.setBookName(bookName);
        book.setAuthor(author.replace("作者：", ""));
        book.setIntro(StrUtil.cleanBlank(HtmlExtractor.extract(document, r.getIntro())));
        // 代理 IP 容易被起点等网站屏蔽，故使用源站封面
        book.setCoverUrl(this.rule.isNeedProxy() ? defaultCoverUrl : CoverUpdater.fetchCover(book, defaultCoverUrl));
        book.setCategory(HtmlExtractor.extract(document, r.getCategory()));
        book.setLatestChapter(HtmlExtractor.extract(document, r.getLatestChapter()));
        book.setLatestChapterUrl(HtmlExtractor.extract(document, r.getLatestChapterUrl()));
        book.setLastUpdateTime(HtmlExtractor.extract(document, r.getLastUpdateTime()).replaceAll("(更新时间|最后更新)：", ""));
        book.setStatus(HtmlExtractor.extract(document, r.getStatus()));

        return ChineseConverter.convert(book, this.rule.getLanguage(), config.getLanguage());
    }

    @SneakyThrows
    private Document fetchDocument(String url, int timeout, String baseUri) {
        try (Response resp = CrawlUtils.request(httpClient, url, timeout);
             InputStream is = resp.body().byteStream()) {
            return Jsoup.parse(is, null, baseUri);
        }
    }

    private Document handleCloudflareBypass(Document document, String url) {
        if (CrawlUtils.hasCf(document)) {
            Assert.isTrue(StrUtil.isNotEmpty(config.getCfBypass()), "🤖 检测到详情页 {} 存在 Cloudflare 真人验证，但未设置 cf-bypass 配置项，故跳过", url);
            Console.log("🤖 检测到详情页 {} 存在 Cloudflare 真人验证，正在尝试绕过...", url);
            String realHtml = HttpUtil.get("%s/html?url=%s".formatted(this.config.getCfBypass(), url));
            document = Jsoup.parse(realHtml);
        }
        return document;
    }

}