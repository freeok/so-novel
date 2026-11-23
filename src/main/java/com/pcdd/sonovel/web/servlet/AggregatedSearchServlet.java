package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.actions.AggregatedSearchAction;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AggregatedSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String name = req.getParameter("kw");
            String searchLimitStr = req.getParameter("searchLimit");
            AggregatedSearchAction.AggregatedOutcome outcome = AggregatedSearchAction.getSearchOutcome(name);
            List<SearchResult> results = outcome.getResults();

            if (StrUtil.isNotBlank(searchLimitStr)) {
                try {
                    int clientLimit = Integer.parseInt(searchLimitStr);
                    int configLimit = AppConfigLoader.APP_CONFIG.getSearchLimit();
                    // 不可超过配置文件限制
                    if (configLimit > 0 && clientLimit > configLimit) {
                        clientLimit = configLimit;
                    }
                    if (clientLimit > 0 && clientLimit < results.size()) {
                        results = results.subList(0, clientLimit);
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("results", results);
            data.put("sourceStatus", outcome.getSourceStatus());
            RespUtils.writeJson(resp, data);
        } catch (Exception e) {
            // 兜底：任何异常都返回 JSON，避免 Jetty 返回 HTML 错误页导致前端解析失败
            RespUtils.writeError(resp, 500, e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

}
