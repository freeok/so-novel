package com.pcdd.sonovel.web.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadProgressSseServlet extends HttpServlet {

    private static final Set<AsyncContext> clients = ConcurrentHashMap.newKeySet();

    public static void sendProgress(String json) {
        byte[] bytes = ("data: " + json + "\n\n").getBytes();

        for (AsyncContext ctx : clients) {
            try {
                // 关键点：使用 synchronized 确保单 client 消息顺序并处理连接状态
                synchronized (ctx) {
                    ServletResponse resp = ctx.getResponse();
                    if (resp != null) {
                        ServletOutputStream out = resp.getOutputStream();
                        out.write(bytes);
                        out.flush(); // 强制推送进度数据
                    } else {
                        clients.remove(ctx);
                    }
                }
            } catch (Exception e) {
                clients.remove(ctx);
                try {
                    ctx.complete();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. 先设置 Header
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/event-stream;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");
        resp.setHeader("X-Accel-Buffering", "no");

        // 2. 开启异步
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(0);

        // 注册监听器
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) {
                clients.remove(asyncContext);
            }

            @Override
            public void onError(AsyncEvent event) {
                clients.remove(asyncContext);
            }

            @Override
            public void onStartAsync(AsyncEvent event) {
            }

            @Override
            public void onTimeout(AsyncEvent event) {
                clients.remove(asyncContext);
                event.getAsyncContext().complete();
            }
        });

        // 3. 获取输出流并建立连接
        ServletOutputStream out = resp.getOutputStream();
        clients.add(asyncContext);

        // 指定浏览器重新发起连接的时间间隔
        out.write("retry: 10000\n".getBytes());
        out.write(": connected\n\n".getBytes());
        out.flush();
    }

}