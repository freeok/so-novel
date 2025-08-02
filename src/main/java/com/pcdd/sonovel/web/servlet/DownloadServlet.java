package com.pcdd.sonovel.web.servlet;

import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.web.DownloadFile;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class DownloadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String bookName = req.getParameter("bookName");
        Integer sourceId = Integer.parseInt(req.getParameter("sourceId"));
        String url = req.getParameter("url");
        String author = req.getParameter("author");
        SearchResult sr = SearchResult.builder().bookName(bookName).sourceId(sourceId).url(url).author(author).build();
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.execute(sr);
        resp.setContentType("text/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        writer.println("{result: 0}");
        writer.flush();
        writer.close();
    }
}
