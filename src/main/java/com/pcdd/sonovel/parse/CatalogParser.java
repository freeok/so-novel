package com.pcdd.sonovel.parse;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.ConfigBean;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.RandomUA;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author pcdd
 * Created at 2024/3/27
 */
public class CatalogParser extends Source {

    private static final int TIMEOUT_MILLS = 30_000;
    private final ConfigBean config;

    public CatalogParser(ConfigBean config) {
        super(config.getSourceId());
        this.config = config;
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
        Rule.Catalog catalogRule = this.rule.getCatalog();
        Rule.Book bookRule = this.rule.getBook();
        // 目录和详情不一定在同一页面
        if (StrUtil.isNotEmpty(catalogRule.getUrl())) {
            // 提取 url 中的变量
            String id = ReUtil.get(bookRule.getUrl(), url, 1);
            url = catalogRule.getUrl().formatted(id);
        }
        // 正数表示忽略前 offset 章，负数表示忽略后 offset 章
        int offset = Optional.ofNullable(catalogRule.getOffset()).orElse(0);

        Document document = getConn(url).get();
        List<Element> elements = document.select(catalogRule.getResult());
        if (offset != 0) {
            if (offset > 0) elements = elements.subList(offset, elements.size());
            if (offset < 0) elements = elements.subList(0, elements.size() + offset);
        }

        List<Chapter> catalog = new ArrayList<>();
        for (int i = start - 1; i < end && i < elements.size(); i++) {
            Chapter build = Chapter.builder()
                    .title(elements.get(i).text())
                    .url(CrawlUtils.normalizeUrl(elements.get(i).attr("href"), this.rule.getUrl()))
                    .chapterNo(i + 1)
                    .build();
            catalog.add(build);
        }

        return catalog;
    }

    private Connection getConn(String url) {
        Connection conn = Jsoup.connect(url)
                .method(CrawlUtils.buildMethod(this.rule.getSearch().getMethod()))
                .header("User-Agent", RandomUA.generate())
                .timeout(TIMEOUT_MILLS);

        // 启用配置文件的代理地址
        if (this.rule.isUseProxy()) {
            String proxyServer = config.getProxyServer();
            String[] split = proxyServer.split(":");
            conn.proxy(split[0], Integer.parseInt(split[1]));
        }

        return conn;
    }

}