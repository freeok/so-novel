package com.pcdd.sonovel.web.util;

import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.web.model.JsonResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.PrintWriter;

@UtilityClass
public class RespUtils {

    public void writeJson(HttpServletResponse resp, Object data) {
        writeJson(resp, JsonResponse.ok(data));
    }

    @SneakyThrows
    public void writeError(HttpServletResponse resp, int code, String message) {
        writeJson(resp, JsonResponse.error(code, message));
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @SneakyThrows
    private void writeJson(HttpServletResponse resp, JsonResponse<?> response) {
        resp.setStatus(response.getCode());
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = resp.getWriter()) {
            writer.println(JSONUtil.toJsonStr(response));
        }
    }

}