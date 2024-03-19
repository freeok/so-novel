package com.pcdd.sonovel.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.Book;
import com.pcdd.sonovel.model.Rule;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;

/**
 * @author pcdd
 */
public class BookParser {

    private final Rule rule;

    public BookParser(int sourceId) {
        // 根据 ruleId 获取对应 json 文件内容
        String jsonStr = ResourceUtil.readUtf8Str("rule/rule" + sourceId + ".json");
        // json 封装进 Rule
        this.rule = JSONUtil.toBean(jsonStr, Rule.class);
    }

    @SneakyThrows
    public Book parse(String url) {
        Rule.Book r = rule.getBook();
        Document document = Jsoup.parse(new URL(url), 30_000);
        String bookName = document.selectXpath(r.getBookName()).text();
        String author = document.selectXpath(r.getAuthor()).attr("content");
        String description = document.selectXpath(r.getDescription()).text();
        String coverUrl = document.selectXpath(r.getCoverUrl()).attr("src");

        Book book = new Book();
        book.setUrl(url);
        book.setBookName(bookName);
        book.setAuthor(author);
        book.setDescription(description);
        book.setCoverUrl(coverUrl);

        return book;
    }

}
