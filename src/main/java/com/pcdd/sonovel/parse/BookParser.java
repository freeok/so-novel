package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.CoverUpdater;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.ContentType;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ChineseConverter;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.JsoupUtils;
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
        Rule.Book r = this.rule.getBook();

        Document document;
        try (Response resp = CrawlUtils.request(httpClient, url, r.getTimeout());
             InputStream is = resp.body().byteStream()) {
            // null 表示自动检测编码
            document = Jsoup.parse(is, null, r.getBaseUri());
        }

        if (CrawlUtils.hasCf(document)) {
            Assert.isTrue(StrUtil.isNotEmpty(config.getCfBypass()), "🤖 检测到详情页 {} 存在 Cloudflare 真人验证，但未设置 cf-bypass 配置项，故跳过", url);
            Console.log("🤖 检测到详情页 {} 存在 Cloudflare 真人验证，正在尝试绕过...", url);
            String realHtml = HttpUtil.get("%s/html?url=%s".formatted(this.config.getCfBypass(), url));
            document = Jsoup.parse(realHtml);
        }

        String bookName = JsoupUtils.selectAndInvokeJs(document, r.getBookName(), getContentType(r.getBookName()));
        String author = JsoupUtils.selectAndInvokeJs(document, r.getAuthor(), getContentType(r.getAuthor()));
        // 以下为非必须属性
        String intro = StrUtil.cleanBlank(JsoupUtils.selectAndInvokeJs(document, r.getIntro(), getContentType(r.getIntro())));
        String defaultCoverUrl = JsoupUtils.selectAndInvokeJs(document, r.getCoverUrl(),
                StrUtil.startWith(r.getCoverUrl(), "meta[") ? ContentType.ATTR_CONTENT : ContentType.ATTR_SRC);
        String category = JsoupUtils.selectAndInvokeJs(document, r.getCategory(), getContentType(r.getCategory()));
        String latestChapter = JsoupUtils.selectAndInvokeJs(document, r.getLatestChapter(), getContentType(r.getLatestChapter()));
        String lastUpdateTime = JsoupUtils.selectAndInvokeJs(document, r.getLastUpdateTime(), getContentType(r.getLastUpdateTime()));
        String status = JsoupUtils.selectAndInvokeJs(document, r.getStatus(), getContentType(r.getStatus()));

        Book book = new Book();
        book.setUrl(url);
        book.setBookName(bookName);
        book.setAuthor(author);
        book.setIntro(intro);
        book.setCoverUrl(CoverUpdater.fetchCover(book, defaultCoverUrl));
        book.setCategory(category);
        book.setLatestChapter(latestChapter);
        book.setLastUpdateTime(lastUpdateTime);
        book.setStatus(status);

        return ChineseConverter.convert(book, this.rule.getLanguage(), config.getLanguage());
    }

    private ContentType getContentType(String query) {
        if (StrUtil.isEmpty(query)) {
            return null;
        }
        return query.startsWith("meta[") ? ContentType.ATTR_CONTENT : ContentType.TEXT;
    }

}