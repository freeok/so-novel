package com.pcdd.sonovel.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.HtmlExtractor;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.*;
import com.pcdd.sonovel.utils.CrawlUtils;
import com.pcdd.sonovel.utils.VirtualThreadLimiter;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.pcdd.sonovel.model.ContentType.ATTR_HREF;
import static com.pcdd.sonovel.model.ContentType.ATTR_VALUE;

public class TocParser extends Source {

    public final OkHttpClient httpClient = HttpClientContext.get();

    public TocParser(AppConfig config) {
        super(config);
    }

    /**
     * 解析全部章节
     */
    public List<Chapter> parseAll(String url) {
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
        String id = ReUtil.getGroup1(StrUtil.subBefore(ruleBook.getUrl(), "@js:", false), url);

        if (id != null) {
            ruleToc.setBaseUri(ruleToc.getBaseUri().formatted(id));
        }
        if (StrUtil.isNotEmpty(ruleToc.getUrl())) {
            url = ruleToc.getUrl().formatted(id);
        }

        Set<String> urls = CollUtil.newLinkedHashSet(url);

        if (StrUtil.isNotBlank(ruleToc.getNextPage())) {
            Document document = fetchDocument(url, ruleToc, ruleBook.getTimeout());
            document = handleCloudflareBypass(document, url);
            extractPaginationUrls(urls, document, ruleToc);
        }

        return parseToc(urls, start, end, ruleToc);
    }

    @SneakyThrows
    private void extractPaginationUrls(Set<String> urls, Document document, Rule.Toc r) {
        Elements elements = HtmlExtractor.select(document, r.getNextPage());
        // 下拉菜单一次性获取所有分页 URL
        if (CollUtil.isNotEmpty(elements) && elements.hasAttr(ATTR_VALUE.getValue())) {
            String attrKey = elements.eachAttr(ATTR_HREF.getValue()).isEmpty()
                    ? ATTR_VALUE.getValue()
                    : ATTR_HREF.getValue();

            List<String> list = elements.stream()
                    .map(el -> el.absUrl(attrKey))
                    .toList();

            // 不能用 addAll：需保证后加入元素覆盖前面已有元素并保持顺序（个别书源 toc.url 不一定是首个 option）
            for (String s : list) {
                urls.remove(s);
                urls.add(s);
            }
            return;
        }
        // 下一页按钮递归获取（覆盖率极低，分页目录基本都通过下拉菜单一次性获取）
        while (true) {
            String href = HtmlExtractor.extract(document, r.getNextPage(), ATTR_HREF);
            String nextUrl = StrUtil.isNotEmpty(href) ? href
                    : HtmlExtractor.extract(document, r.getNextPage(), ATTR_VALUE);
            if (StrUtil.isBlank(nextUrl) || !Validator.isUrl(nextUrl)) break;
            urls.add(nextUrl);

            document = fetchDocument(nextUrl, r, r.getTimeout());
            document = handleCloudflareBypass(document, nextUrl);
            Thread.sleep(CrawlUtils.randomInterval(config));
        }
    }

    @SneakyThrows
    private Document fetchDocument(String url, Rule.Toc r, int timeout) {
        try (Response resp = CrawlUtils.request(httpClient, url, timeout);
             InputStream is = resp.body().byteStream()) {
            return Jsoup.parse(is, null, r.getBaseUri());
        }
    }

    private List<Element> extractElements(Document document, Rule.Toc r) {
        // 处理 ul
        if (StrUtil.isNotEmpty(r.getList())) {
            String tocHtml = HtmlExtractor.extract(document, r.getList(), ContentType.HTML);
            Document tocDoc = Jsoup.parse(tocHtml);
            return HtmlExtractor.select(tocDoc, r.getItem());
        }
        // 处理 ul > li > a
        return HtmlExtractor.select(document, r.getItem());
    }

    private Document handleCloudflareBypass(Document document, String nextUrl) {
        if (CrawlUtils.hasCf(document)) {
            Assert.isTrue(StrUtil.isNotEmpty(config.getCfBypass()), "🤖 检测到目录页 {} 存在 Cloudflare 真人验证，但未设置 cf-bypass 配置项，故跳过", nextUrl);
            Console.log("🤖 检测到目录页 {} 存在 Cloudflare 真人验证，正在尝试绕过...", nextUrl);
            String realHtml = HttpUtil.get("%s/html?url=%s".formatted(this.config.getCfBypass(), nextUrl));
            document = Jsoup.parse(realHtml);
        }
        return document;
    }

    private List<Chapter> parseToc(Set<String> urls, int start, int end, Rule.Toc r) {
        List<String> urlList = new ArrayList<>(urls);
        Map<Integer, List<Element>> pageElements = new ConcurrentHashMap<>();

        try (var limiter = new VirtualThreadLimiter(5)) {
            for (int i = 0; i < urlList.size(); i++) {
                int pageIndex = i;
                String pageUrl = urlList.get(i);
                limiter.submit(() -> {
                    try {
                        Document doc = fetchDocument(pageUrl, r, r.getTimeout());
                        doc = handleCloudflareBypass(doc, pageUrl);
                        pageElements.put(pageIndex, extractElements(doc, r));
                    } catch (Exception e) {
                        Console.error("目录页解析失败: {} - {}", pageUrl, e.getMessage());
                    }
                });
            }
        }

        List<Chapter> toc = new TocList();
        int orderNumber = 1;

        for (int i = 0; i < urlList.size(); i++) {
            List<Element> elements = pageElements.get(i);
            if (elements == null) continue;

            int minIndex = Math.min(end, elements.size());
            if (r.isDesc()) {
                for (int j = minIndex - 1; j >= start - 1; j--) addChapter(elements.get(j), toc, orderNumber++, r);
            } else {
                for (int j = start - 1; j < minIndex; j++) addChapter(elements.get(j), toc, orderNumber++, r);
            }
        }

        return toc;
    }

    private void addChapter(Element el, List<Chapter> toc, int order, Rule.Toc r) {
        toc.add(Chapter.builder()
                .title(el.text())
                .url(HtmlExtractor.extractContent(el, r.getNextPage(), ATTR_HREF))
                .order(order)
                .build());
    }

}