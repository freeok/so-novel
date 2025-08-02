package com.pcdd.sonovel.web.servlet;

import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.action.AggregatedSearchAction;
import com.pcdd.sonovel.model.SearchResult;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SearchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        List<SearchResult> searchResults = AggregatedSearchAction.getSearchResults(name);
        resp.setContentType("text/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        writer.println(JSONUtil.toJsonStr(searchResults));
        writer.flush();
        writer.close();
    }
}
