package com.pcdd.sonovel.web.servlet;

import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.util.ConfigWatcher;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LocalFileDownloadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filename = req.getParameter("filename");
        System.out.println("正在下载文件：" + filename);
        if("".equals(filename) || filename == null){
            PrintWriter writer = resp.getWriter();
            resp.setStatus(500);
            writer.println("参数文件名不可为空");
            writer.flush();
            writer.close();
            return;
        }
        AppConfig config = ConfigWatcher.getConfig();
        File file = new File(config.getDownloadPath() + File.separator + filename);
        if(file.exists()){
            resp.setContentType("text/html");
            resp.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            FileInputStream in = new FileInputStream(file);
            ServletOutputStream outputStream = resp.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            outputStream.close();
        }else{
            PrintWriter writer = resp.getWriter();
            resp.setStatus(500);
            writer.println("文件不存在");
            writer.flush();
            writer.close();
        }
    }
}
