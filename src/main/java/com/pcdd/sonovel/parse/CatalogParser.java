package com.pcdd.sonovel.parse;

import cn.hutool.core.util.URLUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.Rule;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author pcdd
 */
public class CatalogParser extends Source {

    private static final int TIMEOUT_MILLS = 30_000;

    public CatalogParser(int sourceId) {
        super(sourceId);
    }

    /**
     * 解析全章
     */
    public List<Chapter> parse(String url) {
        return parse(url, 1, Integer.MAX_VALUE);
    }

    /**
     * 解析指定范围章节
     */
    @SneakyThrows
    public List<Chapter> parse(String url, int start, int end) {
        Rule.Book book = this.rule.getBook();
        // 正数表示忽略前 offset 章，负数表示忽略后 offset 章
        int offset = Optional.ofNullable(book.getCatalogOffset()).orElse(0);

        Document document = Jsoup.parse(URLUtil.url(url), TIMEOUT_MILLS);
        List<Element> elements = document.select(book.getCatalog());
        if (offset != 0) {
            if (offset > 0) elements = elements.subList(offset, elements.size());
            if (offset < 0) elements = elements.subList(0, elements.size() + offset);
        }

        List<Chapter> catalog = new ArrayList<>();
        for (int i = start - 1; i < end && i < elements.size(); i++) {
            Chapter build = Chapter.builder()
                    .title(elements.get(i).text())
                    .url(this.rule.getUrl() + elements.get(i).attr("href"))
                    .chapterNo(i + 1)
                    .build();
            catalog.add(build);
        }

        return catalog;
    }

}