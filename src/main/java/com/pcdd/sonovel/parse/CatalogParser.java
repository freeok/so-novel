package com.pcdd.sonovel.parse;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import lombok.SneakyThrows;
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

    public CatalogParser(AppConfig config) {
        super(config);
    }

    /**
     * 解析全章
     */
    public List<Chapter> parse(String url) {
        return parse(url, 1, Integer.MAX_VALUE);
    }

    /**
     * 解析指定范围章节
     *
     * @param url 详情页
     */
    @SneakyThrows
    public List<Chapter> parse(String url, int start, int end) {
        Rule.Catalog catalogRule = this.rule.getCatalog();
        Rule.Book bookRule = this.rule.getBook();
        // 目录和详情不在同一页面
        if (StrUtil.isNotEmpty(catalogRule.getUrl())) {
            // 提取 url 中的变量
            String id = ReUtil.getGroup1(bookRule.getUrl(), url);
            url = catalogRule.getUrl().formatted(id);
        }
        // 正数表示忽略前 offset 章，负数表示忽略后 offset 章
        int offset = Optional.ofNullable(catalogRule.getOffset()).orElse(0);
        Document document = jsoupConn(url, TIMEOUT_MILLS).get();
        List<String> urls = new ArrayList<>();

        if (catalogRule.isPagination()) {
            while (true) {
                String href = document.select(catalogRule.getNextPage()).attr("href");
                // 判断 href 是否是有效的 URL
                if (!(Validator.isUrl(href) || StrUtil.startWith(href, "/"))) break;
                // 规范化 URL 并添加到列表
                urls.add(CrawlUtils.normalizeUrl(href, this.rule.getUrl()));
            }
        } else {
            // 目录第一页
            urls.add(url);
        }

        // 目录默认是否降序
        boolean isDesc = this.rule.getCatalog().isDesc();
        int orderNumber = 1;
        List<Chapter> catalog = new ArrayList<>();

        for (String s : urls) {
            Document catalogPage = jsoupConn(s, TIMEOUT_MILLS).get();
            List<Element> elements = catalogPage.select(catalogRule.getResult());
            if (offset != 0) {
                if (offset > 0) elements = elements.subList(offset, elements.size());
                if (offset < 0) elements = elements.subList(0, elements.size() + offset);
            }

            int minIndex = Math.min(end, elements.size());
            if (!isDesc) {
                for (int i = start - 1; i < minIndex; i++) {
                    addChapter(elements.get(i), catalog, orderNumber++);
                }
            } else {
                for (int i = minIndex; --i >= start - 1; ) {
                    addChapter(elements.get(i), catalog, orderNumber++);
                }
            }
        }

        return catalog;
    }

    private void addChapter(Element element, List<Chapter> catalog, int order) {
        catalog.add(Chapter.builder()
                .title(element.text())
                .url(CrawlUtils.normalizeUrl(element.attr("href"), this.rule.getUrl()))
                .order(order)
                .build());
    }

}