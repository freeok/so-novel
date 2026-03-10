package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.action.AggregatedSearchAction;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public class AggregatedSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String name = req.getParameter("kw");
        String searchLimitStr = req.getParameter("searchLimit");
        List<SearchResult> results = AggregatedSearchAction.getSearchResults(name);

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

        RespUtils.writeJson(resp, results);
    }

}