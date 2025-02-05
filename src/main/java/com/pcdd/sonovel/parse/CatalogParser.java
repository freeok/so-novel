package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
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

import static com.pcdd.sonovel.model.ContentType.ATTR_HREF;

public class CatalogParser extends Source {

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
        Rule.Book ruleBook = this.rule.getBook();
        Rule.Catalog ruleCatalog = this.rule.getCatalog();

        // 目录和详情不在同一页面
        if (StrUtil.isNotEmpty(ruleCatalog.getUrl())) {
            String id = ReUtil.getGroup1(ruleBook.getUrl(), url);
            url = ruleCatalog.getUrl().formatted(id);
        }

        List<String> urls = CollUtil.toList(url);
        Document document = jsoup(url)
                .timeout(ruleCatalog.getTimeout())
                .get();

        if (ruleCatalog.isPagination()) {
            extractPaginationUrls(urls, document, ruleCatalog);
        }

        return parseCatalog(urls, start, end, ruleCatalog);
    }

    // TODO 优化，一次性获取分页 URL，而不是递归获取
    private void extractPaginationUrls(List<String> urls, Document document, Rule.Catalog r) throws Exception {
        while (true) {
            String href = CrawlUtils.selectAndInvokeJs(document, r.getNextPage(), ATTR_HREF);
            if (!(Validator.isUrl(href) || StrUtil.startWith(href, "/"))) break;
            String catalogUrl = CrawlUtils.normalizeUrl(href, this.rule.getUrl());
            urls.add(catalogUrl);
            document = jsoup(catalogUrl)
                    .timeout(r.getTimeout())
                    .get();
        }
    }

    // TODO 优化，改为多线程
    private List<Chapter> parseCatalog(List<String> urls, int start, int end, Rule.Catalog r) throws Exception {
        List<Chapter> catalog = new ArrayList<>();
        boolean isDesc = r.isDesc();
        int orderNumber = 1;
        int offset = r.getOffset() != null ? r.getOffset() : 0;

        for (String s : urls) {
            Document catalogPage = jsoup(s)
                    .timeout(r.getTimeout())
                    .get();
            List<Element> elements = CrawlUtils.select(catalogPage, r.getResult());
            if (offset != 0) {
                elements = adjustElementsByOffset(elements, offset);
            }
            int minIndex = Math.min(end, elements.size());
            if (isDesc) {
                for (int i = minIndex - 1; i >= start - 1; i--) {
                    addChapter(elements.get(i), catalog, orderNumber++, r);
                }
            } else {
                for (int i = start - 1; i < minIndex; i++) {
                    addChapter(elements.get(i), catalog, orderNumber++, r);
                }
            }
        }

        return catalog;
    }

    private List<Element> adjustElementsByOffset(List<Element> elements, int offset) {
        if (offset > 0) return elements.subList(offset, elements.size());
        if (offset < 0) return elements.subList(0, elements.size() + offset);
        return elements;
    }

    private void addChapter(Element el, List<Chapter> catalog, int order, Rule.Catalog r) {
        String url = CrawlUtils.getStrAndInvokeJs(el, r.getNextPage(), ATTR_HREF);
        catalog.add(Chapter.builder()
                .title(el.text())
                .url(CrawlUtils.normalizeUrl(url, this.rule.getUrl()))
                .order(order)
                .build());
    }

}