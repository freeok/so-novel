package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.web.service.ConfigService;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class ConfigServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        AppConfig cfg = AppConfigLoader.APP_CONFIG;
        RespUtils.writeJson(resp, cfg);
    }

    /**
     * 保存服务器配置，请求体：{ "group": { "key": value } }，写回 config.ini（保留注释）
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String body = readBody(req);
            Map<String, Map<String, Object>> changes = JSONUtil.toBean(body,
                    new TypeReference<Map<String, Map<String, Object>>>() {
                    }, false);
            AppConfig cfg = ConfigService.save(changes);
            RespUtils.writeJson(resp, cfg);
        } catch (Exception e) {
            RespUtils.writeError(resp, 400, e.getMessage());
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        return req.getReader().lines().reduce("", (a, b) -> a + b);
    }

}
