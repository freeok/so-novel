package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.handler.SearchResultsHandler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.parser.SearchParser;
import com.pcdd.sonovel.utils.SourceUtils;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 独立搜索：仅在指定的单个书源中搜索（对应 TUI 的 w.独立搜索）
 */
public class SingleSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String kw = req.getParameter("kw");
        String sourceIdStr = req.getParameter("sourceId");

        if (StrUtil.isBlank(kw)) {
            RespUtils.writeError(resp, 400, "搜索关键字(kw)不能为空");
            return;
        }
        if (StrUtil.isBlank(sourceIdStr)) {
            RespUtils.writeError(resp, 400, "缺少书源 sourceId 参数");
            return;
        }

        try {
            int sourceId = Integer.parseInt(sourceIdStr);
            // 校验规则存在（不存在会抛 IllegalArgumentException）
            SourceUtils.getRule(sourceId);

            AppConfig cfg = BeanUtil.copyProperties(AppConfigLoader.APP_CONFIG, AppConfig.class);
            cfg.setSourceId(sourceId);

            List<SearchResult> results = new SearchParser(cfg).parse(kw);
            RespUtils.writeJson(resp, SearchResultsHandler.filterAndSort(results, kw));
        } catch (NumberFormatException e) {
            RespUtils.writeError(resp, 400, "书源 sourceId 非法: " + sourceIdStr);
        } catch (Exception e) {
            RespUtils.writeError(resp, 500, e.getMessage());
        }
    }

}
