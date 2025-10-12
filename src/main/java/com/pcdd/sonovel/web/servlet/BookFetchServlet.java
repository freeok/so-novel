package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.ConfigWatcher;
import com.pcdd.sonovel.util.SourceUtils;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BookFetchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String bookUrl = req.getParameter("url");
            SearchResult sr = SearchResult.builder()
                    .url(bookUrl)
                    .sourceId(SourceUtils.getSource(bookUrl).getId())
                    .build();
            downloadFileToServer(sr);
        } catch (Exception e) {
            RespUtils.writeError(resp, 500, "下载失败: " + e.getMessage());
        }
    }

    private void downloadFileToServer(SearchResult sr) {
        AppConfig config = ConfigWatcher.getConfig();
        config.setSourceId(sr.getSourceId());
        Console.log("<== 正在获取章节目录...");
        new Crawler(config).crawl(sr.getUrl());
    }

}