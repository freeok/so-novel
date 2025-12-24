package com.pcdd.sonovel.web;


import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.web.servlet.*;
import org.eclipse.jetty.ee11.servlet.DefaultServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

import static org.fusesource.jansi.AnsiRenderer.render;

public class WebServer {

    public void start() {
        int port = AppConfigLoader.APP_CONFIG.getWebPort();
        Server server = new Server(port);
        ServletContextHandler context = createServletContext();
        registerServlets(context);
        server.setHandler(context);
        try {
            server.start();
            Console.log("SoNovel {}", "v" + AppConfigLoader.APP_CONFIG.getVersion());
            Console.log(render("✔ Web server started.", "green"));
            Console.log(render("➜ Local: http://localhost:{}/", "blue"), port);
            server.join();
        } catch (Exception e) {
            Console.error(e, render("✖ Startup failed.", "red"));
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
        context.addServlet(VersionServlet.class, "/version");

        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        // 不显示目录列表，但子文件依然可访问
        staticHolder.setInitParameter("dirAllowed", "false");
        context.addServlet(staticHolder, "/");
    }

}