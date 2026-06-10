package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.util.RandomUA;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SuggestionServlet extends HttpServlet {

    private static final String BAIDU_SUGGEST_URL = "https://www.baidu.com/sugrec?prod=pc&wd=";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String kw = req.getParameter("kw");
        if (StrUtil.isBlank(kw)) {
            RespUtils.writeJson(resp, List.of());
            return;
        }

        try {
            String url = BAIDU_SUGGEST_URL + URLEncoder.encode(kw, StandardCharsets.UTF_8);
            String body = HttpRequest.get(url)
                    .header(Header.USER_AGENT, RandomUA.generate())
                    .timeout(5000)
                    .execute()
                    .body();

            List<String> suggestions = parseSuggestions(body);
            RespUtils.writeJson(resp, suggestions);
        } catch (Exception e) {
            RespUtils.writeError(resp, 500, "获取搜索建议失败");
        }
    }

    private List<String> parseSuggestions(String body) {
        if (StrUtil.isBlank(body) || !JSONUtil.isTypeJSON(body)) {
            return List.of();
        }

        JSONArray arr = JSONUtil.parseObj(body).getJSONArray("g");
        if (arr == null || arr.isEmpty()) {
            return List.of();
        }

        return arr.stream()
                .map(item -> JSONUtil.parseObj(item).getStr("q"))
                .filter(StrUtil::isNotBlank)
                .limit(10)
                .toList();
    }
}