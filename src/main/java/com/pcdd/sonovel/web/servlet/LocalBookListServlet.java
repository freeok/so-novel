package com.pcdd.sonovel.web.servlet;

import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.web.model.LocalBookItem;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalBookListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        File dir = new File(AppConfigLoader.APP_CONFIG.getDownloadPath());
        File[] files = dir.listFiles(File::isFile);

        List<LocalBookItem> list = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                LocalBookItem localBookItem = new LocalBookItem();
                localBookItem.setName(f.getName());
                localBookItem.setSize(f.length());
                localBookItem.setTimestamp(f.lastModified());
                list.add(localBookItem);
            }
        }

        RespUtils.writeJson(resp, list);
    }

}