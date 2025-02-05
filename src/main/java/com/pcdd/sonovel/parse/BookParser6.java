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
public class BookParser6 extends Source {

    public BookParser6(AppConfig config) {
        super(config);
    }

    @SneakyThrows
    public Book parse(String url) {
        Rule.Book r = this.rule.getBook();
        Document document = jsoup(url)
                .timeout(r.getTimeout())
                .get();
        String bookName = document.select(r.getBookName()).text();
        String author = document.select(r.getAuthor()).text();
        String intro = document.select(r.getIntro()).text();
        intro = StrUtil.cleanBlank(intro);
        String coverUrl = document.select(r.getCoverUrl()).attr("src");

        Book book = new Book();
        book.setUrl(url);
        book.setBookName(bookName);
        book.setAuthor(author);
        book.setIntro(intro);
        book.setCoverUrl(CrawlUtils.normalizeUrl(coverUrl, this.rule.getUrl()));
        book.setCoverUrl(CoverUpdater.fetchQidian(book));

        return ChineseConverter.convert(book, this.rule.getLanguage(), config.getLanguage());
    }

}