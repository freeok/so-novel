package com.pcdd.sonovel.web.servlet;

import com.pcdd.sonovel.action.AggregatedSearchAction;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public class AggregatedSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String name = req.getParameter("kw");
        List<SearchResult> results = AggregatedSearchAction.getSearchResults(name);
        RespUtils.writeJson(resp, results);
    }

}