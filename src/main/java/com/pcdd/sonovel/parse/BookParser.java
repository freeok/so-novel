package com.pcdd.sonovel.parse;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.CoverUpdater;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.ContentType;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.SneakyThrows;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
public class BookParser extends Source {

    public BookParser(AppConfig config) {
        super(config);
    }

    @SneakyThrows
    public Book parse(String url) {
        Rule.Book r = this.rule.getBook();

        Document document;
        try (Response resp = request(url)) {
            document = Jsoup.parse(resp.body().string(), r.getBaseUri());
        }

        String bookName = JsoupUtils.selectAndInvokeJs(document, r.getBookName(), getContentType(r.getBookName()));
        String author = JsoupUtils.selectAndInvokeJs(document, r.getAuthor(), getContentType(r.getAuthor()));
        String intro = StrUtil.cleanBlank(JsoupUtils.selectAndInvokeJs(document, r.getIntro(), getContentType(r.getIntro())));
        String coverUrl = JsoupUtils.selectAndInvokeJs(document, r.getCoverUrl(),
                StrUtil.startWith(r.getCoverUrl(), "meta[") ? ContentType.ATTR_CONTENT : ContentType.ATTR_SRC);
        // 以下为非必须属性
        String category = JsoupUtils.selectAndInvokeJs(document, r.getCategory(), getContentType(r.getCategory()));
        String latestChapter = JsoupUtils.selectAndInvokeJs(document, r.getLatestChapter(), getContentType(r.getLatestChapter()));
        String lastUpdateTime = JsoupUtils.selectAndInvokeJs(document, r.getLastUpdateTime(), getContentType(r.getLastUpdateTime()));
        String status = JsoupUtils.selectAndInvokeJs(document, r.getStatus(), getContentType(r.getStatus()));
        String wordCount = JsoupUtils.selectAndInvokeJs(document, r.getWordCount(), getContentType(r.getWordCount()));

        Book book = new Book();
        book.setUrl(url);
        book.setBookName(bookName);
        book.setAuthor(author);
        book.setIntro(intro);
        book.setCoverUrl(CoverUpdater.fetchCover(book, coverUrl));
        book.setCategory(category);
        book.setLatestChapter(latestChapter);
        book.setLastUpdateTime(lastUpdateTime);
        book.setStatus(status);
        book.setWordCount(wordCount);

        return ChineseConverter.convert(book, this.rule.getLanguage(), config.getLanguage());
    }

    private ContentType getContentType(String query) {
        if (StrUtil.isEmpty(query)) {
            return null;
        }
        return query.startsWith("meta[") ? ContentType.ATTR_CONTENT : ContentType.TEXT;
    }

}