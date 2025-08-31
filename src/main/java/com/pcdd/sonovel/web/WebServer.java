package com.pcdd.sonovel.web;


import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.util.ConfigWatcher;
import com.pcdd.sonovel.web.servlet.*;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

public class WebServer {

    public void start() {
        int port = ConfigWatcher.getConfig().getWebPort();
        Server server = new Server(port);
        ServletContextHandler context = createServletContext();
        registerServlets(context);
        server.setHandler(context);
        try {
            server.start();
            Console.log("Web server started on port {}", port);
            server.join();
        } catch (Exception e) {
            Console.error("Failed to start Web server on port {}: {}", port, e.getMessage());
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
        context.addServlet(BookFetchServlet.class, "/book-fetch");
        context.addServlet(BookDownloadServlet.class, "/book-download");
        context.addServlet(LocalBookListServlet.class, "/local-books");
        context.addServlet(AggregatedSearchServlet.class, "/search/aggregated");
        context.addServlet(DownloadProgressSseServlet.class, "/download-progress");

        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        // 不显示目录列表，但子文件依然可访问
        staticHolder.setInitParameter("dirAllowed", "false");
        context.addServlet(staticHolder, "/");
    }

}