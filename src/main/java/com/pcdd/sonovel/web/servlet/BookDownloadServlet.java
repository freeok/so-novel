package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BookDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            SearchResult sr = SearchResult.builder()
                    .bookName(req.getParameter("bookName"))
                    .sourceId(Integer.parseInt(req.getParameter("sourceId")))
                    .url(req.getParameter("url"))
                    .author(req.getParameter("author"))
                    .build();

            downloadFile(sr);
            RespUtils.writeJson(resp, "开始下载");
        } catch (Exception e) {
            RespUtils.writeError(resp, 500, "下载失败: " + e.getMessage());
        }
    }

    private void downloadFile(SearchResult sr) {
        AppConfig config = ConfigUtils.defaultConfig();
        config.setSourceId(sr.getSourceId());
        Console.log("<== 正在获取章节目录...");
        new Crawler(config).crawl(sr.getUrl());
    }

}