package com.pcdd.sonovel.parser;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.ConsoleTable;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.HtmlExtractor;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.ContentType;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.Rule.Book;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.utils.ChineseConverter;
import com.pcdd.sonovel.utils.CrawlUtils;
import com.pcdd.sonovel.utils.JsCaller;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.*;

import static org.fusesource.jansi.AnsiRenderer.render;

/**
 * @author pcdd
 * Created at 2024/3/23
 */
public class SearchParser extends Source {

    private static final int TEXT_LIMIT_LENGTH = 30;
    private final OkHttpClient httpClient = HttpClientContext.get();

    public SearchParser(AppConfig config) {
        super(config);
    }

    @SneakyThrows
    public List<SearchResult> parse(String keyword) {
        Rule.Search r = this.rule.getSearch();

        if (r == null) {
            Console.log(render("<== 书源 {} 不支持搜索", "yellow"), config.getSourceId());
            return Collections.emptyList();
        }
        if (this.rule.isDisabled()) {
            Console.error(render("<== 书源 {} ({}) 已被禁用", "yellow"), this.rule.getId(), this.rule.getName());
            return Collections.emptyList();
        }

        Document document;
        try {
            String searchUrl = processUrl(r.getUrl(), keyword);
            String referer = URI.create(searchUrl).resolve("/").toString();
            Request.Builder builder = new Request.Builder()
                    .addHeader(Header.REFERER.toString(), referer)
                    .url(searchUrl);

            if (StrUtil.isNotBlank(r.getCookies())) {
                builder.addHeader("Cookie", r.getCookies());
            }
            if ("post".equalsIgnoreCase(r.getMethod())) {
                builder = builder.post(CrawlUtils.buildData(r.getData(), keyword));
            }

            try (Response resp = CrawlUtils.request(httpClient, builder, r.getTimeout())) {
                String body = processResultWithJs(resp.peekBody(Long.MAX_VALUE).string(), r.getResult());
                document = Jsoup.parse(body, r.getBaseUri());
            }

            if (CrawlUtils.hasCf(document)) {
                Assert.isTrue(StrUtil.isNotEmpty(config.getCfBypass()), "🤖 检测到搜索页 {} 存在 Cloudflare 真人验证，但未设置 cf-bypass 配置项，故跳过", searchUrl);
                Console.log("🤖 检测到搜索页 {} 存在 Cloudflare 真人验证，正在尝试绕过...", searchUrl);
                String html = HttpUtil.get("%s/html?url=%s".formatted(this.config.getCfBypass(), searchUrl));
                document = Jsoup.parse(html);
            }

        } catch (Exception e) {
            Console.error(render("<== 书源 {} ({}) 搜索解析出错: {}", "red"),
                    this.rule.getId(), this.rule.getName(), e.getMessage());
            return Collections.emptyList();
        }

        List<SearchResult> firstPageResults = getSearchResults(document, r);
        // 搜索结果无分页
        if (StrUtil.isBlank(r.getNextPage())) {
            return firstPageResults;
        }
        // 注意，css 或 xpath 的查询结果必须为多个 a 元素
        Elements nextPageUrls = HtmlExtractor.select(document, r.getNextPage());
        // 只有一页时，底部可能没有分页菜单
        if (nextPageUrls.isEmpty()) {
            return firstPageResults;
        }
        // 分页搜索结果的 URL，不含首页
        Set<String> urls = new LinkedHashSet<>();
        // 一次性获取分页 URL，不考虑逐个点击下一页的情况
        for (Element e : nextPageUrls) {
            String href = e.absUrl("href");
            // 中文解码，便于调试
            urls.add(URLUtil.decode(href));
        }
        // 使用并行流处理分页 URL
        List<SearchResult> additionalResults = urls.parallelStream()
                .flatMap(url -> getSearchResults(fetchDocument(url, r), r).stream())
                .toList();
        // 合并，不去重（去重用 union）
        List<SearchResult> searchResults = CollUtil.unionAll(firstPageResults, additionalResults);
        int limit = config.getSearchLimit() == -1 ? Integer.MAX_VALUE : config.getSearchLimit();
        // TODO 优化，需要几条获取几条，而不是一次性获取然后截取
        return CollUtil.sub(searchResults, 0, limit);
    }

    @SneakyThrows
    private Document fetchDocument(String url, Rule.Search r) {
        try (Response resp = CrawlUtils.request(httpClient, url, r.getTimeout())) {
            String body = processResultWithJs(resp.peekBody(Long.MAX_VALUE).string(), r.getResult());
            return Jsoup.parse(body, r.getBaseUri());
        }
    }

    private List<SearchResult> getSearchResults(Document document, Rule.Search r) {
        List<SearchResult> list = new ArrayList<>();
        try {
            String resultSelector = stripJs(r.getResult());
            Elements resultEls = HtmlExtractor.select(document, resultSelector);

            // 部分书源完全匹配时会直接跳转到详情页（搜索结果为空 && 书名不为空），故需要构造搜索结果
            if (resultEls.isEmpty() && !HtmlExtractor.select(document, this.rule.getBook().getBookName()).isEmpty()) {
                String bookUrl = document.location();
                BookParser bookParser = new BookParser(config);
                Book book = bookParser.parse(bookUrl);

                if (StrUtil.isBlank(book.getBookName())) {
                    return Collections.emptyList();
                }

                SearchResult sr = SearchResult.builder()
                        .sourceId(this.rule.getId())
                        .sourceName(this.rule.getName())
                        .url(bookUrl)
                        .bookName(book.getBookName())
                        .author(book.getAuthor())
                        .latestChapter(book.getLatestChapter())
                        .lastUpdateTime(book.getLastUpdateTime())
                        .build();
                list.add(ChineseConverter.convert(sr, this.rule.getLanguage(), config.getLanguage()));
                Thread.sleep(CrawlUtils.randomInterval(config));
                return list;
            }
            // 只获取前 N 条搜索记录
            List<Element> limitResultEls = resultEls.stream()
                    .filter(e -> StrUtil.isNotEmpty(HtmlExtractor.extract(e, r.getBookName())))
                    .limit(config.getSearchLimit() == -1 ? Integer.MAX_VALUE : config.getSearchLimit())
                    .toList();

            for (Element el : limitResultEls) {
                // jsoup 不支持一次性获取属性的值
                String href = HtmlExtractor.extract(el, r.getBookName(), ContentType.ATTR_HREF);
                String bookName = HtmlExtractor.extract(el, r.getBookName());
                if (bookName.isEmpty()) continue;

                SearchResult sr = SearchResult.builder()
                        .sourceId(this.rule.getId())
                        .sourceName(this.rule.getName())
                        .url(href)
                        .bookName(bookName)
                        // 以下为非必须属性
                        .author(HtmlExtractor.extract(el, r.getAuthor()))
                        .category(HtmlExtractor.extract(el, r.getCategory()))
                        .latestChapter(HtmlExtractor.extract(el, r.getLatestChapter()))
                        .lastUpdateTime(HtmlExtractor.extract(el, r.getLastUpdateTime()))
                        .status(HtmlExtractor.extract(el, r.getStatus()))
                        .wordCount(HtmlExtractor.extract(el, r.getWordCount()))
                        .build();

                list.add(ChineseConverter.convert(sr, this.rule.getLanguage(), config.getLanguage()));
            }
        } catch (Exception e) {
            Console.error(e);
            return Collections.emptyList();
        }

        return list;
    }

    public void printSearchResult(List<SearchResult> results) {
        Rule.Search r = this.rule.getSearch();
        if (r == null || CollUtil.isEmpty(results)) return;

        ConsoleTable consoleTable = ConsoleTable.create();
        // 根据首个结果判断哪些属性列存在，统一构造表头
        SearchResult first = results.getFirst();
        List<String> cols = new ArrayList<>();
        List<String> titles = ListUtil.toList("序号", "书名");
        if (addColumnIfNotEmpty(cols, r.getAuthor(), first.getAuthor())) titles.add("作者");
        if (addColumnIfNotEmpty(cols, r.getCategory(), first.getCategory())) titles.add("类别");
        if (addColumnIfNotEmpty(cols, r.getLatestChapter(), first.getLatestChapter())) titles.add("最新章节");
        if (addColumnIfNotEmpty(cols, r.getLastUpdateTime(), first.getLastUpdateTime())) titles.add("更新时间");
        if (addColumnIfNotEmpty(cols, r.getStatus(), first.getStatus())) titles.add("状态");
        if (addColumnIfNotEmpty(cols, r.getWordCount(), first.getWordCount())) titles.add("总字数");
        consoleTable.addHeader(ArrayUtil.toArray(titles, String.class));

        for (int i = 1; i <= results.size(); i++) {
            SearchResult sr = results.get(i - 1);
            List<String> row = ListUtil.toList(String.valueOf(i), sr.getBookName());
            addColumnIfNotEmpty(row, r.getAuthor(), sr.getAuthor());
            addColumnIfNotEmpty(row, r.getCategory(), sr.getCategory());
            addColumnIfNotEmpty(row, r.getLatestChapter(), StrUtil.subPre(sr.getLatestChapter(), TEXT_LIMIT_LENGTH));
            addColumnIfNotEmpty(row, r.getLastUpdateTime(), ReUtil.replaceAll(sr.getLastUpdateTime(), "\\d{2}:\\d{2}(:\\d{2})?", ""));
            addColumnIfNotEmpty(row, r.getStatus(), sr.getStatus());
            addColumnIfNotEmpty(row, r.getWordCount(), sr.getWordCount());
            consoleTable.addBody(ArrayUtil.toArray(row, String.class));
        }

        Console.table(consoleTable);
    }

    // 辅助方法：只有非空的情况下才添加列
    private boolean addColumnIfNotEmpty(List<String> list, String rule, String value) {
        if (StrUtil.isNotEmpty(rule)) {
            list.add(StrUtil.isNotEmpty(value) ? value : "");
            return true;
        }
        return false;
    }

    public static void printAggregateSearchResult(List<SearchResult> results) {
        ConsoleTable consoleTable = ConsoleTable.create().addHeader("序号", "书名", "作者", "最新章节", "最后更新时间", "书源");
        for (int i = 1; i <= results.size(); i++) {
            SearchResult sr = results.get(i - 1);

            if (sr.getLatestChapter() == null) {
                sr.setLatestChapter("/");
            } else {
                sr.setLatestChapter(StrUtil.subPre(sr.getLatestChapter(), TEXT_LIMIT_LENGTH));
            }
            if (sr.getLastUpdateTime() == null) {
                sr.setLastUpdateTime("/");
            }

            consoleTable.addBody(
                    String.valueOf(i),
                    sr.getBookName(),
                    sr.getAuthor(),
                    sr.getLatestChapter(),
                    ReUtil.replaceAll(sr.getLastUpdateTime(), "\\d{2}:\\d{2}(:\\d{2})?", ""),
                    String.valueOf(sr.getSourceId())
            );
        }
        Console.table(consoleTable);
    }

    // 若 url 含 @js:，则 JS 接收 keyword 返回完整 URL；否则直接格式化
    private static String processUrl(String url, String keyword) {
        if (url != null && url.contains("@js:")) {
            return JsCaller.call(StrUtil.subAfter(url, "@js:", false), keyword);
        }
        return url.formatted(keyword);
    }

    // 若 result 含 @js:，则 JS 接收响应体并返回转换后的 HTML
    private static String processResultWithJs(String body, String result) {
        if (result != null && result.contains("@js:")) {
            return JsCaller.call(StrUtil.subAfter(result, "@js:", false), body);
        }
        return body;
    }

    // 剥离 result 中的 @js: 部分，仅保留 CSS/XPath 选择器
    private static String stripJs(String result) {
        if (result != null && result.contains("@js:")) {
            return StrUtil.subBefore(result, "@js:", false);
        }
        return result;
    }

}