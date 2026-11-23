package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.web.service.RuleFileService;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 书源可视化管理：单条查询 / 新增 / 修改 / 删除 / 启停。
 * 直接写回激活规则文件并刷新缓存。
 */
public class SourceManageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            RespUtils.writeJson(resp, RuleFileService.getRawRule(requireId(req)));
        } catch (IllegalArgumentException e) {
            RespUtils.writeError(resp, 400, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            int newId = RuleFileService.addRule(readBody(req));
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("id", newId);
            res.put("message", "新增成功");
            RespUtils.writeJson(resp, res);
        } catch (IllegalArgumentException | IOException e) {
            RespUtils.writeError(resp, 400, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try {
            RuleFileService.updateRule(requireId(req), readBody(req));
            RespUtils.writeJson(resp, "修改成功");
        } catch (IllegalArgumentException | IOException e) {
            RespUtils.writeError(resp, 400, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try {
            RuleFileService.deleteRule(requireId(req));
            RespUtils.writeJson(resp, "删除成功");
        } catch (IllegalArgumentException e) {
            RespUtils.writeError(resp, 400, e.getMessage());
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        try {
            int id = requireId(req);
            String field = req.getParameter("field");
            boolean value = Boolean.parseBoolean(req.getParameter("value"));
            if ("searchDisabled".equals(field)) {
                RuleFileService.toggleSearchDisabled(id, value);
            } else {
                RuleFileService.toggleRuleDisabled(id, value);
            }
            RespUtils.writeJson(resp, "状态已更新");
        } catch (IllegalArgumentException e) {
            RespUtils.writeError(resp, 400, e.getMessage());
        }
    }

    private int requireId(HttpServletRequest req) {
        String id = req.getParameter("id");
        if (StrUtil.isBlank(id)) {
            throw new IllegalArgumentException("缺少书源 id 参数");
        }
        try {
            int v = Integer.parseInt(id);
            if (v < 1) {
                throw new IllegalArgumentException("书源 id 非法: " + id);
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("书源 id 非法: " + id);
        }
    }

    private JSONObject readBody(HttpServletRequest req) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining("\n"));
        if (!JSONUtil.isTypeJSON(body)) {
            throw new IllegalArgumentException("请求体必须是书源规则 JSON 对象");
        }
        return JSONUtil.parseObj(body);
    }

}
