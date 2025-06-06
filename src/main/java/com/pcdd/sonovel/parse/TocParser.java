package com.pcdd.sonovel.parse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.ContentType;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.JsoupUtils;
import com.pcdd.sonovel.util.TocList;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.pcdd.sonovel.model.ContentType.ATTR_HREF;
import static com.pcdd.sonovel.model.ContentType.ATTR_VALUE;

public class TocParser extends Source {

    public final OkHttpClient client = HttpClientContext.get();

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
        // 非 / 开头的 path 需设置 baseUri
        if (StrUtil.isNotEmpty(ruleToc.getBaseUri())) {
            String id = ReUtil.getGroup1(StrUtil.subBefore(ruleBook.getUrl(), "@js:", false), url);
            ruleToc.setBaseUri(ruleToc.getBaseUri().formatted(id));
        }
        // 目录分页 url
        Set<String> urls = new LinkedHashSet<>();
        urls.add(url);

        Document document;
        try (Response resp = CrawlUtils.request(client, url, ruleBook.getTimeout())) {
            document = Jsoup.parse(resp.body().string(), ruleToc.getBaseUri());
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
            String attrKey = elements.eachAttr(ATTR_HREF.getValue()).isEmpty()
                    ? ATTR_VALUE.getValue()
                    : ATTR_HREF.getValue();

            List<String> list = elements.stream()
                    .map(el -> el.absUrl(attrKey))
                    .toList();

            // 不能用 addAll，这里要保证后加入元素覆盖前面已存在的元素并保持顺序，因为 toc.url 不一定是 select 的首个 option (见书源20)
            for (String s : list) {
                urls.remove(s);
                urls.add(s);
            }

            return;
        }
        // 以下代码覆盖率可能为 0，因为分页的目录基本全都是通过下拉菜单一次性获取的
        // 递归获取分页 URL（模拟点击下一页）
        while (true) {
            String nextUrl = Opt.ofNullable(JsoupUtils.selectAndInvokeJs(document, r.getNextPage(), ATTR_HREF))
                    .filter(StrUtil::isNotEmpty)
                    .orElse(JsoupUtils.selectAndInvokeJs(document, r.getNextPage(), ATTR_VALUE));
            if (StrUtil.isEmpty(nextUrl) || !Validator.isUrl(nextUrl)) break;
            urls.add(nextUrl);

            try (Response resp = CrawlUtils.request(client, nextUrl, r.getTimeout())) {
                document = Jsoup.parse(resp.body().string(), this.rule.getToc().getBaseUri());
            }

            Thread.sleep(CrawlUtils.randomInterval(config));
        }
    }

    /**
     * @param urls 分页目录的 url
     */
    @SneakyThrows
    private List<Chapter> parseToc(Set<String> urls, int start, int end, Rule.Toc r) {
        List<Chapter> toc = new TocList();
        boolean isDesc = r.isDesc();
        int orderNumber = 1;
        int offset = r.getOffset() != null ? r.getOffset() : 0;

        // TODO 多线程优化
        for (String url : urls) {
            Document document;
            try (Response resp = CrawlUtils.request(client, url, r.getTimeout())) {
                document = Jsoup.parse(resp.body().string(), this.rule.getToc().getBaseUri());
            }

            // TODO rule.toc.item 实现 JS 语法，在此调用比 addChapter 性能更好
            List<Element> elements;
            // 处理 ul
            if (StrUtil.isNotEmpty(r.getList())) {
                String tocHtml = JsoupUtils.selectAndInvokeJs(document, r.getList(), ContentType.HTML);
                Document tocDocument = Jsoup.parse(tocHtml);
                elements = JsoupUtils.select(tocDocument, r.getItem());
            } else { // 处理 ul > li > a
                elements = JsoupUtils.select(document, r.getItem());
            }

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

        return toc;
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
                .url(url)
                .order(order)
                .build());
    }

}