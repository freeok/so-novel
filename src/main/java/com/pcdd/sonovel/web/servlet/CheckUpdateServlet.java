package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.VersionUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.utils.RandomUA;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 检查更新（对应 TUI 的 x.检查更新）：查询 GitHub 最新 release，返回版本对比，不自动下载。
 */
public class CheckUpdateServlet extends HttpServlet {

    private static final String RELEASE_URL = "https://api.github.com/repos/freeok/so-novel/releases";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try (HttpResponse r = HttpUtil.createGet(RELEASE_URL)
                .timeout(8000)
                .header(Header.USER_AGENT, RandomUA.generate())
                .execute()) {

            if (!r.isOk()) {
                RespUtils.writeError(resp, 502, "GitHub 返回非成功状态: " + r.getStatus());
                return;
            }

            JSONArray arr = JSONUtil.parseArray(r.body());
            JSONObject latest = JSONUtil.parseObj(arr.getFirst());
            String current = "v" + AppConfigLoader.sys().getStr("version");
            String latestVer = latest.getStr("tag_name");

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("currentVersion", current);
            data.put("latestVersion", latestVer);
            data.put("hasUpdate", VersionUtil.isLessThan(current, latestVer));
            data.put("releaseUrl", latest.getStr("html_url"));
            data.put("releaseName", latest.getStr("name"));
            data.put("releaseNotes", latest.getStr("body"));
            data.put("publishedAt", latest.getStr("published_at"));
            RespUtils.writeJson(resp, data);
        } catch (Exception e) {
            RespUtils.writeError(resp, 502, "检查更新失败（可能无法访问 GitHub）: " + e.getMessage());
        }
    }

}
