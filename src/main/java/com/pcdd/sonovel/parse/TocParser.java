package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.JsoupUtils;
import lombok.SneakyThrows;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.pcdd.sonovel.model.ContentType.ATTR_HREF;
import static com.pcdd.sonovel.model.ContentType.ATTR_VALUE;

public class TocParser extends Source {

    public TocParser(AppConfig config) {
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
        Rule.Toc ruleToc = this.rule.getToc();
        Rule.Book ruleBook = this.rule.getBook();

        // 目录和详情不在同一页面
        if (StrUtil.isNotEmpty(ruleToc.getUrl())) {
            String id = ReUtil.getGroup1(StrUtil.subBefore(ruleBook.getUrl(), "@js:", false), url);
            url = ruleToc.getUrl().formatted(id);
        }
        // 目录分页 url，需要对 url 进行排序，原因是首个页面不一定是 select 的第一个 option
        Set<String> urls = new TreeSet<>();
        urls.add(url);

        Document document;
        try (Response resp = request(url)) {
            document = Jsoup.parse(resp.body().string());
        }

        if (ruleToc.isPagination()) {
            extractPaginationUrls(urls, document, ruleToc);
        }

        return parseToc(urls, start, end, ruleToc);
    }

    @SneakyThrows
    private void extractPaginationUrls(Set<String> urls, Document document, Rule.Toc r) {
        Elements elements = JsoupUtils.select(document, r.getNextPage());
        // 一次性获取分页 URL（下拉菜单）
        if (CollUtil.isNotEmpty(elements) && elements.hasAttr(ATTR_VALUE.getValue())) {
            List<String> list = elements.eachAttr(ATTR_HREF.getValue()).isEmpty()
                    ? elements.eachAttr(ATTR_VALUE.getValue())
                    : elements.eachAttr(ATTR_HREF.getValue());
            list.forEach(s -> urls.add(CrawlUtils.normalizeUrl(s, this.rule.getUrl())));
            return;
        }
        // 以下代码覆盖率可能为 0，因为分页的目录基本全都是通过下拉菜单一次性获取的
        // 递归获取分页 URL（模拟点击下一页）
        while (true) {
            String nextUrl = Opt.ofNullable(JsoupUtils.selectAndInvokeJs(document, r.getNextPage(), ATTR_HREF))
                    .filter(StrUtil::isNotEmpty)
                    .orElse(JsoupUtils.selectAndInvokeJs(document, r.getNextPage(), ATTR_VALUE));
            if (StrUtil.isEmpty(nextUrl) || !Validator.isUrl(nextUrl)) break;
            nextUrl = CrawlUtils.normalizeUrl(nextUrl, this.rule.getUrl());
            urls.add(nextUrl);

            try (Response resp = request(nextUrl)) {
                document = Jsoup.parse(resp.body().string());
            }

            Thread.sleep(CrawlUtils.randomInterval(config));
        }
    }

    // TODO 优化，改为多线程
    @SneakyThrows
    private List<Chapter> parseToc(Set<String> urls, int start, int end, Rule.Toc r) {
        List<Chapter> toc = new ArrayList<>();
        boolean isDesc = r.isDesc();
        int orderNumber = 1;
        int offset = r.getOffset() != null ? r.getOffset() : 0;

        for (String url : urls) {
            Document document;
            try (Response resp = request(url)) {
                document = Jsoup.parse(resp.body().string());
            }

            // TODO rule.toc.result 实现 JS 语法，在此调用比 addChapter 性能更好
            List<Element> elements = JsoupUtils.select(document, r.getResult());
            if (offset != 0) {
                elements = adjustElementsByOffset(elements, offset);
            }
            int minIndex = Math.min(end, elements.size());
            if (isDesc) {
                for (int i = minIndex - 1; i >= start - 1; i--) {
                    addChapter(elements.get(i), toc, orderNumber++, r);
                }
            } else {
                for (int i = start - 1; i < minIndex; i++) {
                    addChapter(elements.get(i), toc, orderNumber++, r);
                }
            }
        }

        // 不要根据章节名中的小写数字或大写数字对 urls 进行排序（此方法仍不可靠，因为某些章节名的数字不按顺序，例如番外 1）

        // 根据章节名去重
        return CollUtil.distinct(toc, Chapter::getTitle, false);
    }

    private List<Element> adjustElementsByOffset(List<Element> elements, int offset) {
        if (elements.size() < offset) {
            return elements;
        }
        if (offset > 0) {
            return elements.subList(offset, elements.size());
        }
        if (offset < 0) {
            return elements.subList(0, elements.size() + offset);
        }
        return elements;
    }

    private void addChapter(Element el, List<Chapter> toc, int order, Rule.Toc r) {
        String url = JsoupUtils.getStrAndInvokeJs(el, r.getNextPage(), ATTR_HREF);
        toc.add(Chapter.builder()
                .title(el.text())
                .url(CrawlUtils.normalizeUrl(url, this.rule.getUrl()))
                .order(order)
                .build());
    }

}