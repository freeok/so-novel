package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.util.SourceUtils;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Set;

public class BookFetchServlet extends HttpServlet {

    private static final Set<String> ALLOWED_FORMATS = Set.of("epub", "txt", "html", "pdf");
    private static final Set<String> ALLOWED_LANGUAGES = Set.of("zh_cn", "zh_tw", "zh_hant");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String bookUrl = req.getParameter("url");
            String format = req.getParameter("format");
            String language = req.getParameter("language");
            String concurrencyStr = req.getParameter("concurrency");
            int id = SourceUtils.getRule(bookUrl).getId();

            if (StrUtil.isNotBlank(format) && !ALLOWED_FORMATS.contains(format.toLowerCase())) {
                RespUtils.writeError(resp, 400, "不支持的下载格式: " + format + "，可选: epub, txt, html, pdf");
                return;
            }

            if (StrUtil.isNotBlank(language) && !ALLOWED_LANGUAGES.contains(language.toLowerCase())) {
                RespUtils.writeError(resp, 400, "不支持的语言: " + language + "，可选: zh_CN, zh_TW, zh_Hant");
                return;
            }

            Integer concurrency = null;
            if (StrUtil.isNotBlank(concurrencyStr)) {
                concurrency = Integer.parseInt(concurrencyStr);
                int configConcurrency = AppConfigLoader.APP_CONFIG.getConcurrency();
                int maxAllowed = configConcurrency > 0 ? configConcurrency : 50;
                if (concurrency < 1 || concurrency > maxAllowed) {
                    RespUtils.writeError(resp, 400, "并发数须在 1~" + maxAllowed + " 之间");
                    return;
                }
            }

            SearchResult sr = SearchResult.builder()
                    .sourceId(id)
                    .url(bookUrl)
                    .build();

            downloadFileToServer(sr, format, language, concurrency);
        } catch (Exception e) {
            RespUtils.writeError(resp, 500, "下载失败: " + e.getMessage());
        }
    }

    private void downloadFileToServer(SearchResult sr, String format, String language, Integer concurrency) {
        AppConfig cfg = BeanUtil.copyProperties(AppConfigLoader.APP_CONFIG, AppConfig.class);
        cfg.setSourceId(sr.getSourceId());

        if (StrUtil.isNotBlank(format)) {
            cfg.setExtName(format.toLowerCase());
        }
        if (StrUtil.isNotBlank(language)) {
            cfg.setLanguage(language);
        }
        if (concurrency != null) {
            cfg.setConcurrency(concurrency);
        }

        Console.log("<== 正在获取章节目录...");
        new Crawler(cfg).crawl(sr.getUrl());
    }

}