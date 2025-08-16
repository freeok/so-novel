package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.pcdd.sonovel.util.ConfigWatcher;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;

/**
 * 从服务器下载文件到客户端
 */
public class BookDownloadServlet extends HttpServlet {

    private static final int BUFFER_SIZE = 1024;

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
        File file = new File(ConfigWatcher.getConfig().getDownloadPath(), filename);

        if (!file.exists()) {
            RespUtils.writeError(resp, 404, "文件不存在");
            return;
        }

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment;filename=" + URLUtil.encode(filename));

        try (FileInputStream in = new FileInputStream(file);
             ServletOutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

}