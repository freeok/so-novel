package com.pcdd.sonovel.web.servlet;

import com.pcdd.sonovel.util.SourceUtils;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SourceListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        boolean check = req.getRequestURI().endsWith("/check");
        RespUtils.writeJson(resp, check
                ? SourceUtils.getActivatedSourcesWithAvailabilityCheck()
                : SourceUtils.getActivatedSources());
    }

}