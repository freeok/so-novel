package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 从服务器下载文件到客户端
 */
public class BookDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String filename = req.getParameter("filename");

        if (StrUtil.isBlank(filename)) {
            RespUtils.writeError(resp, 400, "参数文件名不可为空");
            return;
        }

        downloadFileToLocal(resp, filename);
    }

    @SneakyThrows
    private void downloadFileToLocal(HttpServletResponse resp, String filename) {
        File file = new File(ConfigUtils.defaultConfig().getDownloadPath(), filename);

        if (!file.exists()) {
            RespUtils.writeError(resp, 404, "文件不存在");
            return;
        }

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment;filename=" + URLUtil.encode(filename));
        resp.setHeader("Content-Length", String.valueOf(file.length()));

        Files.copy(Paths.get(file.getAbsolutePath()), resp.getOutputStream());
    }

}