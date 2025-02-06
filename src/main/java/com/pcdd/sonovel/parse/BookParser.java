package com.pcdd.sonovel.parse;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.CoverUpdater;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.ContentType;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.SneakyThrows;
import org.jsoup.nodes.Document;

/**
 * @author pcdd
 * Created at 2024/3/17
 */
public class BookParser extends Source {

    public static final String CONTENT = "content";

    public BookParser(AppConfig config) {
        super(config);
    }

    @SneakyThrows
    public Book parse(String url) {
        Rule.Book r = this.rule.getBook();
        Document document = jsoup(url)
                .timeout(r.getTimeout())
                .get();

        String bookName = JsoupUtils.selectAndInvokeJs(document, r.getBookName(), getContentType(r.getBookName()));
        String author = JsoupUtils.selectAndInvokeJs(document, r.getAuthor(), getContentType(r.getAuthor()));
        String intro = StrUtil.cleanBlank(JsoupUtils.selectAndInvokeJs(document, r.getIntro(), getContentType(r.getIntro())));
        String coverUrl = JsoupUtils.selectAndInvokeJs(document, r.getCoverUrl(),
                r.getCoverUrl().startsWith("meta[") ? ContentType.ATTR_CONTENT : ContentType.ATTR_SRC);
        // 以下为非必须属性
        String latestChapter = JsoupUtils.selectAndInvokeJs(document, r.getLatestChapter(), getContentType(r.getLatestChapter()));
        String latestUpdate = JsoupUtils.selectAndInvokeJs(document, r.getLatestUpdate(), getContentType(r.getLatestUpdate()));

        Book book = new Book();
        book.setUrl(url);
        book.setBookName(bookName);
        book.setAuthor(author);
        book.setIntro(intro);
        book.setCoverUrl(CoverUpdater.fetchCover(book, CrawlUtils.normalizeUrl(coverUrl, this.rule.getUrl())));
        book.setLatestChapter(latestChapter);
        book.setLatestUpdate(latestUpdate);

        return ChineseConverter.convert(book, this.rule.getLanguage(), config.getLanguage());
    }

    private ContentType getContentType(String query) {
        return query.startsWith("meta[") ? ContentType.ATTR_CONTENT : ContentType.TEXT;
    }

}