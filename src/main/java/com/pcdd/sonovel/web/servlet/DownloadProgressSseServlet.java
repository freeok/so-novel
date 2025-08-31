package com.pcdd.sonovel.web.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadProgressSseServlet extends HttpServlet {

    private static final Set<AsyncContext> clients = ConcurrentHashMap.newKeySet();

    // 供后台线程调用的推送方法
    public static void sendProgress(String json) {
        String msg = "data: " + json + "\n\n";
        for (AsyncContext ctx : clients) {
            try {
                HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
                PrintWriter out = resp.getWriter();
                out.write(msg);
                out.flush();
            } catch (Exception e) {
                clients.remove(ctx);
                ctx.complete();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/event-stream;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");

        // 开启异步，永不超时
        final AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(0);
        clients.add(asyncContext);

        PrintWriter out = resp.getWriter();
        // 冒号开头的行，表示注释
        out.write(": this is a test stream\n\n");
        out.flush();
    }

}