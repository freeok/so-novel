package com.pcdd.sonovel.web;


import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.util.ConfigWatcher;
import com.pcdd.sonovel.web.servlet.AggregatedSearchServlet;
import com.pcdd.sonovel.web.servlet.BookDownloadServlet;
import com.pcdd.sonovel.web.servlet.LocalBookDownloadServlet;
import com.pcdd.sonovel.web.servlet.LocalBookListServlet;
import com.pcdd.sonovel.web.socket.ChapterDownloadProgressWS;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.time.Duration;
import java.util.List;

public class WebServer {

    public void start() {
        int port = ConfigWatcher.getConfig().getWebPort();
        Server server = new Server(port);
        ServletContextHandler context = createServletContext();

        registerServlets(context);
        registerWebSocketEndpoints(context);

        server.setHandler(context);
        try {
            server.start();
            Console.log("✅ Web server started on port {}", port);
            server.join();
        } catch (Exception e) {
            Console.error("❌ Failed to start Web server on port {}: {}", port, e.getMessage());
            throw new IllegalStateException("Web server failed to start", e);
        }
    }

    private ServletContextHandler createServletContext() {
        ServletContextHandler context = new ServletContextHandler("/");
        context.setBaseResource(ResourceFactory.of(context)
                .newResource(WebServer.class.getClassLoader().getResource("static")));
        return context;
    }

    private void registerServlets(ServletContextHandler context) {
        context.addServlet(BookDownloadServlet.class, "/book-download");
        context.addServlet(LocalBookDownloadServlet.class, "/local-book-download");
        context.addServlet(LocalBookListServlet.class, "/local-books");
        context.addServlet(AggregatedSearchServlet.class, "/search/aggregated");

        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        // 不显示目录列表，但子文件依然可访问
        staticHolder.setInitParameter("dirAllowed", "false");
        context.addServlet(staticHolder, "/");
    }

    private void registerWebSocketEndpoints(ServletContextHandler context) {
        JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, container) -> {
            // 一个 ws 连接在多久没有任何通信后自动关闭
            container.setDefaultMaxSessionIdleTimeout(Duration.ofMinutes(30).toMillis());
            // 服务器允许接收的文本消息最大大小 (64KB)
            container.setDefaultMaxTextMessageBufferSize(65536);
            container.addEndpoint(ServerEndpointConfig.Builder
                    .create(ChapterDownloadProgressWS.class, "/ws/book/progress")
                    .subprotocols(List.of("ws"))
                    .build());
        });
    }

}