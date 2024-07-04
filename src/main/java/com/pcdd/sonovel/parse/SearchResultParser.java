package com.pcdd.sonovel.parse;

import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author pcdd
 */
public class SearchResultParser extends Parser {

    public SearchResultParser(int sourceId) {
        super(sourceId);
    }

    @SneakyThrows
    public List<SearchResult> parse(String keyword) {
        Rule.Search search = this.rule.getSearch();
        Connection connect = Jsoup.connect(search.getUrl());
        // 搜索结果页DOM
        Document document = connect.data(search.getParamName().getKeyword(), keyword).post();
        Elements elements = document.select(search.getResult());

        List<SearchResult> list = new ArrayList<>();
        for (Element element : elements) {
            // jsoup 不支持一次性获取属性的值
            String url = element.select(search.getBookName()).attr("href");
            String bookName = element.select(search.getBookName()).text();
            String latestChapter = element.select(search.getLatestChapter()).text();
            String author = element.select(search.getAuthor()).text();
            String update = element.select(search.getUpdate()).text();

            // 排除第一个 tr（表头）
            // 如果存在任何一个字符串为空字符串，则执行相应的操作
            if (Stream.of(url, bookName, latestChapter, author, update).anyMatch(String::isEmpty)) {
                continue;
            }
            SearchResult build = SearchResult.builder()
                    .url(url)
                    .bookName(bookName)
                    .latestChapter(latestChapter)
                    .author(author)
                    .latestUpdate(update)
                    .build();

            list.add(build);
        }

        return list;
    }


}
