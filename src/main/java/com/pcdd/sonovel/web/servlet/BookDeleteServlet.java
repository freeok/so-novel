package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;

public class BookDeleteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String filename = req.getParameter("filename");
        if (StrUtil.isBlank(filename)) {
            RespUtils.writeError(resp, 400, "filename 参数不能为空");
            return;
        }

        File dir = new File(AppConfigLoader.APP_CONFIG.getDownloadPath());
        File target = new File(dir, filename);

        if (!target.exists()) {
            RespUtils.writeError(resp, 404, "文件不存在");
            return;
        }

        if (!target.delete()) {
            RespUtils.writeError(resp, 500, "文件删除失败");
            return;
        }

        RespUtils.writeJson(resp, null);
    }

}