package com.pcdd.sonovel.web.servlet;

import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public class ConfigServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        AppConfig cfg = AppConfigLoader.APP_CONFIG;
        Map<String, Object> configInfo = Map.of(
                "searchLimit", cfg.getSearchLimit(),
                "concurrency", cfg.getConcurrency()
        );
        RespUtils.writeJson(resp, configInfo);
    }

}
