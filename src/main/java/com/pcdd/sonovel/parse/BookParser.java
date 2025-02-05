package com.pcdd.sonovel.parse;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.CoverUpdater;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
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
        // 从 head > meta 获取
        String bookName = document.select(r.getBookName()).attr(CONTENT);
        String author = document.select(r.getAuthor()).attr(CONTENT);
        String intro = document.select(r.getIntro()).attr(CONTENT);
        intro = StrUtil.cleanBlank(intro);
        String coverUrl;
        if (r.getCoverUrl().startsWith("meta[property=")) {
            coverUrl = document.select(r.getCoverUrl()).attr(CONTENT);
        } else {
            coverUrl = document.select(r.getCoverUrl()).attr("src");
        }
        // 以下为非必须属性，需判空，否则抛出 org.jsoup.helper.ValidationException: String must not be empty
        String latestChapter = StrUtil.isNotEmpty(r.getLatestChapter())
                ? document.select(r.getLatestChapter()).attr(CONTENT)
                : null;
        String latestUpdate = StrUtil.isNotEmpty(r.getLatestUpdate())
                ? document.select(r.getLatestUpdate()).attr(CONTENT)
                : null;

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

}