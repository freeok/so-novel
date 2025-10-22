package com.pcdd.sonovel.parse;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.util.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.handle.SearchResultsHandler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.ContentType;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.JsCaller;
import com.pcdd.sonovel.util.JsoupUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 全本小说网，搜索特殊处理 TODO 写进规则
 *
 * @author pcdd
 * Created at 2024/3/23
 */
public class SearchParserQuanben5 extends Source {

    private final OkHttpClient httpClient = HttpClientContext.get();

    public SearchParserQuanben5(AppConfig config) {
        super(config);
    }

    public List<SearchResult> parse(String keyword) {
        try {
            Rule.Search ruleSearch = rule.getSearch();
            String js = ResourceUtil.readUtf8Str("js/quanben5.js");
            Object paramB = JsCaller.callFunction(js, "getParamB", keyword);

            String url = String.format(ruleSearch.getUrl(), keyword, paramB);
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Referer", "https://quanben5.com/search.html")
                    .build();

            try (Response resp = httpClient.newCall(request).execute()) { // 使用client.newCall(request).execute()
                String body = resp.body().string();
                String processedBody = HtmlUtil.unescape(UnicodeUtil.toString(body))
                        .replace("\\r", "")
                        .replace("\\n", "")
                        .replace("\\t", "")
                        .replace("\\/", "/")
                        .replace("\\\"", "'");

                String jsonContent = ReUtil.getGroup0("\\{(.*?)\\}", processedBody);
                String content = StrUtil.subBetween(jsonContent, "\"content\":", "}");
                String html = StrUtil.strip(content, "\"");

                Document document = Jsoup.parse(html, ruleSearch.getBaseUri());
                return SearchResultsHandler.sort(getSearchResults(document));
            }
        } catch (Exception e) {
            Console.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<SearchResult> getSearchResults(Document document) {
        Rule.Search ruleSearch = this.rule.getSearch();
        List<SearchResult> list = new ArrayList<>();
        Elements elements = document.select(ruleSearch.getResult());

        for (Element element : elements) {
            String href = JsoupUtils.selectAndInvokeJs(element, ruleSearch.getBookName(), ContentType.ATTR_HREF);
            String bookName = JsoupUtils.selectAndInvokeJs(element, ruleSearch.getBookName());
            String author = JsoupUtils.selectAndInvokeJs(element, ruleSearch.getAuthor());

            SearchResult sr = SearchResult.builder()
                    .sourceId(this.rule.getId())
                    .url(href)
                    .bookName(bookName)
                    .author(author)
                    .build();

            list.add(ChineseConverter.convert(sr, this.rule.getLanguage(), config.getLanguage()));
        }

        return list;
    }

}