package com.pcdd.sonovel.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Opt;
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
        // 详情页书籍 ID
        String id = ReUtil.getGroup1(StrUtil.subBefore(ruleBook.getUrl(), "@js:", false), url);

        // 相对路径 (href 开头不是 /) 需设置 toc.baseUri
        if (id != null) {
            ruleToc.setBaseUri(ruleToc.getBaseUri().formatted(id));
        }
        // 目录和详情不在同一页面需设置 toc.url
        if (StrUtil.isNotEmpty(ruleToc.getUrl())) {
            url = ruleToc.getUrl().formatted(id);
        }

        // 目录分页 url
        Set<String> urls = CollUtil.newLinkedHashSet(url);

        if (StrUtil.isNotBlank(ruleToc.getNextPage())) {
            Document document;
            try (Response resp = CrawlUtils.request(httpClient, url, ruleBook.getTimeout());
                 InputStream is = resp.body().byteStream()) {
                // null 表示自动检测编码
                document = Jsoup.parse(is, null, ruleToc.getBaseUri());
            }
            document = handleCloudflareBypass(document, url);
            extractPaginationUrls(urls, document, ruleToc);
        }

        return parseToc(urls, start, end, ruleToc);
    }

    @SneakyThrows
    private void extractPaginationUrls(Set<String> urls, Document document, Rule.Toc r) {
        Elements elements = HtmlExtractor.select(document, r.getNextPage());
        // 一次性获取分页 URL (下拉菜单)
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
        // 递归获取分页目录 URL (下一页按钮的链接)。以下代码覆盖率可能为 0，因为分页目录的链接基本全都是通过下拉菜单一次性获取的
        while (true) {
            String nextUrl = Opt.ofNullable(HtmlExtractor.extract(document, r.getNextPage(), ATTR_HREF))
                    .filter(StrUtil::isNotEmpty)
                    .orElse(HtmlExtractor.extract(document, r.getNextPage(), ATTR_VALUE));
            if (StrUtil.isBlank(nextUrl) || !Validator.isUrl(nextUrl)) break;
            urls.add(nextUrl);

            try (Response resp = CrawlUtils.request(httpClient, nextUrl, r.getTimeout());
                 InputStream is = resp.body().byteStream()) {
                // null 表示自动检测编码
                document = Jsoup.parse(is, null, this.rule.getToc().getBaseUri());
            }
            document = handleCloudflareBypass(document, nextUrl);

            Thread.sleep(CrawlUtils.randomInterval(config));
        }
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

    /**
     * @param urls 分页目录的 url
     */
    private List<Chapter> parseToc(Set<String> urls, int start, int end, Rule.Toc r) {
        List<String> urlList = new ArrayList<>(urls);
        Map<Integer, List<Element>> pageElements = new ConcurrentHashMap<>();

        /*
         * 仅分页目录生效，7140 章测试结果
         * concurrent: time
         * 0:  9.32s
         * 1:  8.85s
         * 5:  3.26s
         * 10: 2.3s
         * 20: 1.94s
         */
        try (var limiter = new VirtualThreadLimiter(5)) {
            for (int i = 0; i < urlList.size(); i++) {
                int pageIndex = i;
                String pageUrl = urlList.get(i);
                limiter.submit(() -> {
                    try {
                        Document document;
                        try (Response resp = CrawlUtils.request(httpClient, pageUrl, r.getTimeout());
                             InputStream is = resp.body().byteStream()) {
                            document = Jsoup.parse(is, null, rule.getToc().getBaseUri());
                        }
                        document = handleCloudflareBypass(document, pageUrl);

                        // TODO wxsy.net rule.toc.item 实现 JS 语法比在此调用 addChapter 性能更好
                        List<Element> elements;
                        if (StrUtil.isNotEmpty(r.getList())) { // 处理 ul
                            String tocHtml = HtmlExtractor.extract(document, r.getList(), ContentType.HTML);
                            Document tocDocument = Jsoup.parse(tocHtml);
                            elements = HtmlExtractor.select(tocDocument, r.getItem());
                        } else { // 处理 ul > li > a
                            elements = HtmlExtractor.select(document, r.getItem());
                        }
                        pageElements.put(pageIndex, elements);
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

        // 不要根据章节名中的小写数字或大写数字对 urls 进行排序（此方法不可靠，因为某些章节名的数字不按顺序，例如番外 1 并不是第一章）
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