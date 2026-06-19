package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.io.File;

public class BookDeleteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String filename = req.getParameter("filename");

        if (StrUtil.isBlank(filename)) {
            RespUtils.writeError(resp, 400, "filename 参数不能为空");
            return;
        }

        deleteDownloadedBook(resp, filename);
    }

    @SneakyThrows
    private void deleteDownloadedBook(HttpServletResponse resp, String filename) {
        File file = new File(AppConfigLoader.APP_CONFIG.getDownloadPath(), filename).getCanonicalFile();
        String baseDir = new File(AppConfigLoader.APP_CONFIG.getDownloadPath()).getCanonicalPath();

        if (!file.getPath().startsWith(baseDir + File.separator)) {
            RespUtils.writeError(resp, 403, "非法路径");
            return;
        }

        if (!file.exists()) {
            RespUtils.writeError(resp, 404, "文件不存在");
            return;
        }

        if (!file.delete()) {
            RespUtils.writeError(resp, 500, "文件删除失败");
            return;
        }

        RespUtils.writeJson(resp, null);
    }

}