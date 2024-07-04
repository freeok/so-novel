package com.pcdd.sonovel.parse;

import cn.hutool.core.util.URLUtil;
import cn.hutool.setting.dialect.Props;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.util.Settings;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pcdd
 */
public class CatalogParser extends Parser {

    public static final String INDEX_URL;

    // 加载配置文件参数
    static {
        Props sys = Settings.sys();
        INDEX_URL = sys.getStr("index_url");
    }

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
        Document document = Jsoup.parse(URLUtil.url(url), 30_000);
        Elements elements = document.select(this.rule.getBook().getCatalog());
        List<Chapter> catalog = new ArrayList<>();

        for (int i = start - 1; i < end && i < elements.size(); i++) {
            Chapter build = Chapter.builder()
                    .title(elements.get(i).text())
                    .url(INDEX_URL + elements.get(i).attr("href"))
                    .chapterNo(i + 1)
                    .build();
            catalog.add(build);
        }

        return catalog;
    }

}