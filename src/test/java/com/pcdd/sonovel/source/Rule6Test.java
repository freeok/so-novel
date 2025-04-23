package com.pcdd.sonovel.source;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.script.ScriptUtil;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ConfigUtils;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author pcdd
 * Created at 2025/1/30
 */
class Rule6Test {

    public static final AppConfig config = ConfigUtils.config();

    @Test
    @SneakyThrows
    void rule6() {
        String script = ResourceUtil.readUtf8Str("js/rule-6.js");
        Object invoke = ScriptUtil.invoke(script, "getParamB", "吞噬星空");
        System.out.println("getParamB = " + invoke);
    }

    @Test
    void crackSearch6() {
        String kw = "我吃西红柿";
        String js = ResourceUtil.readUtf8Str("js/rule-6.js");
        Object key = ScriptUtil.invoke(js, "getParamB", kw);

        Source source = new Source(config);
        Rule.Search ruleSearch = source.rule.getSearch();
        String param = ruleSearch.getData().formatted(kw, key);
        JSONObject obj = JSONUtil.parseObj(param);
        System.out.println(JSONUtil.toJsonPrettyStr(obj));

        Map<String, String> map = JSONUtil.toBean(param, Map.class);
        HttpRequest req = HttpRequest
                .get(ruleSearch.getUrl())
                .header("Referer", ruleSearch.getUrl() + "search.html")
                .formStr(map);

        System.out.println(req.form());
        String body = req.execute().body();
        String s = UnicodeUtil.toString(body);
        String s2 = HtmlUtil.unescape(s)
                .replace("\\r", "")
                .replace("\\n", "")
                .replace("\\t", "")
                .replace("\\/", "/")
                .replace("\\\"", "'");
        String s4 = ReUtil.getGroup0("\\{(.*?)\\}", s2);
        JSONObject jsonObject = JSONUtil.parseObj(s4);
        String html = jsonObject.getStr("content");
        System.out.println(Jsoup.parse(html));
    }

}