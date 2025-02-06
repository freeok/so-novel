package com.pcdd.sonovel.parse;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.hutool.script.ScriptUtil;
import com.pcdd.sonovel.convert.ChineseConverter;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.handle.SearchResultsHandler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.ContentType;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.CrawlUtils;
import com.pcdd.sonovel.util.JsoupUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 书源 6 搜索特殊处理
 *
 * @author pcdd
 * Created at 2024/3/23
 */
public class SearchResultParser6 extends Source {

    public SearchResultParser6(AppConfig config) {
        super(config);
    }

    public List<SearchResult> parse(String keyword) {
        Rule.Search r = this.rule.getSearch();
        String js = ResourceUtil.readUtf8Str("js/rule-6.js");
        Object key = ScriptUtil.invoke(js, "getParamB", keyword);
        String param = r.getData().formatted(keyword, key.toString());
        Map<String, String> map = JSONUtil.toBean(param, new TypeReference<>() {
        }, true);

        HttpRequest req = HttpRequest
                .get(this.rule.getUrl())
                .timeout(r.getTimeout())
                .header("Referer", r.getUrl())
                .formStr(map);
        if (config.getProxyEnabled() == 1)
            req.setHttpProxy(config.getProxyHost(), config.getProxyPort());

        try (HttpResponse resp = req.execute()) {
            String body = resp.body();
            String s = UnicodeUtil.toString(body);
            String s2 = HtmlUtil.unescape(s)
                    .replace("\\r", "")
                    .replace("\\n", "")
                    .replace("\\t", "")
                    .replace("\\/", "/")
                    .replace("\\\"", "'");
            String s3 = ReUtil.getGroup0("\\{(.*?)\\}", s2);
            String beginIndex = "\"content\":";
            String ans = s3.substring(s3.indexOf(beginIndex) + beginIndex.length(), s3.lastIndexOf("}"));
            List<SearchResult> firstPageResults = getSearchResults(Jsoup.parse(ans));
            return SearchResultsHandler.handle(firstPageResults);

        } catch (Exception e) {
            Console.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<SearchResult> getSearchResults(Document document) {
        Rule.Search r = this.rule.getSearch();
        List<SearchResult> list = new ArrayList<>();
        Elements elements = document.select(r.getResult());

        for (Element element : elements) {
            String href = JsoupUtils.selectAndInvokeJs(element, r.getBookName(), ContentType.ATTR_HREF);
            String bookName = JsoupUtils.selectAndInvokeJs(element, r.getBookName());
            String author = JsoupUtils.selectAndInvokeJs(element, r.getAuthor());

            SearchResult sr = SearchResult.builder()
                    .url(CrawlUtils.normalizeUrl(href, this.rule.getUrl()))
                    .bookName(bookName)
                    .author(author)
                    .build();

            list.add(ChineseConverter.convert(sr, this.rule.getLanguage(), config.getLanguage()));
        }

        return list;
    }

}