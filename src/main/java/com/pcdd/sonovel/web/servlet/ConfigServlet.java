package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
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

    /**
     * 密钥掩码占位符：读配置时用它替换真实密钥，写配置时遇到它表示「保持不变」。
     * 前端不需要（也不应该）拿到真实密钥。真源在 {@link com.pcdd.sonovel.model.AiConfig#SECRET_MASK}
     */
    private static final String SECRET_MASK = com.pcdd.sonovel.model.AiConfig.SECRET_MASK;

    /**
     * 返回配置快照，其中 AI 密钥做掩码处理，避免明文密钥经 HTTP 暴露给页面/浏览器历史
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        AppConfig cfg = AppConfigLoader.APP_CONFIG;
        RespUtils.writeJson(resp, maskKey(cfg));
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
            dropUnchangedSecret(changes);
            AppConfig cfg = ConfigService.save(changes);
            RespUtils.writeJson(resp, maskKey(cfg));
        } catch (Exception e) {
            RespUtils.writeError(resp, 400, e.getMessage());
        }
    }

    /**
     * 把 ai.apiKey 换成掩码，并告知前端「是否已配置 / 是否来自环境变量」。
     * 以内存中的真实配置对象为准（密钥优先级已在 AppConfigLoader 算好），不重复推断。
     */
    private JSONObject maskKey(AppConfig cfg) {
        JSONObject json = JSONUtil.parseObj(JSONUtil.toJsonStr(cfg));
        JSONObject ai = json.getJSONObject("ai");
        if (ai != null) {
            boolean hasKey = cfg.getAi() != null && cfg.getAi().hasKey();
            ai.set("apiKey", hasKey ? SECRET_MASK : "");
            ai.set("hasApiKey", hasKey);
            // 密钥来自环境变量时，config.ini 里其实是空的；提示前端以免误清空
            ai.set("apiKeyFromEnv", StrUtil.isNotBlank(System.getenv("SONOVEL_AI_KEY")));
        }
        return json;
    }

    /**
     * 掩码值代表用户没有改动密钥，必须丢弃该字段，否则会把真实密钥写成一串星号。
     * 同时：若 config.ini 里本来没密钥、而密钥来自环境变量，也无需写入文件。
     */
    private void dropUnchangedSecret(Map<String, Map<String, Object>> changes) {
        Map<String, Object> ai = changes.get("ai");
        if (ai == null) return;
        Object raw = ai.get("api-key");
        if (raw != null && SECRET_MASK.equals(raw.toString().trim())) {
            ai.remove("api-key");
        }
        // 空串表示用户主动清空
    }

    private String readBody(HttpServletRequest req) throws IOException {
        return req.getReader().lines().reduce("", (a, b) -> a + b);
    }

}
