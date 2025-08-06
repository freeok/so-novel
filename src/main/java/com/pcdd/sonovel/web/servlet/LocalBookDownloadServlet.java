package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.URLUtil;
import com.pcdd.sonovel.util.ConfigWatcher;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LocalBookDownloadServlet extends HttpServlet {

    private static final int BUFFER_SIZE = 1024;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filename = req.getParameter("filename");
        if (filename == null || filename.isBlank()) {
            RespUtils.writeError(resp, 400, "参数文件名不可为空");
            return;
        }

        File file = new File(ConfigWatcher.getConfig().getDownloadPath(), filename);
        if (!file.exists()) {
            RespUtils.writeError(resp, 404, "文件不存在");
            return;
        }

        // ServletUtil.write(resp, file);
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