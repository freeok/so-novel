package com.pcdd.sonovel.web.servlet;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.util.ConfigWatcher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LocalFileListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppConfig config = ConfigWatcher.getConfig();
        File file = new File(config.getDownloadPath());
        File[] files = file.listFiles();
        JSONArray jsonArray = new JSONArray();
        assert files != null;
        for(File f : files){
            if(f.isFile()){
                JSONObject json = new JSONObject();
                json.set("name", f.getName());
                json.set("size", f.length());
                json.set("time", f.lastModified());
                jsonArray.add(json);
            }
        }
        resp.setContentType("text/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        writer.println(jsonArray.toJSONString(0));
        writer.flush();
        writer.close();
    }
}
