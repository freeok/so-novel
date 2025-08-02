package com.pcdd.sonovel.web;

import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.util.ConfigWatcher;
import com.pcdd.sonovel.web.servlet.DownloadServlet;
import com.pcdd.sonovel.web.servlet.LocalFileDownloadServlet;
import com.pcdd.sonovel.web.servlet.LocalFileListServlet;
import com.pcdd.sonovel.web.servlet.SearchServlet;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.util.List;

public class WebServer {
    Server server;

    public void init() {
        int port = ConfigWatcher.getConfig().getWebPort();
        server = new Server(port);


        ServletContextHandler handler = new ServletContextHandler("/");

        ServletHolder fileListHolder = new ServletHolder("FileList", LocalFileListServlet.class);
        handler.addServlet(fileListHolder, "/file/list");

        ServletHolder fileDownloadHolder = new ServletHolder("FileDownload", LocalFileDownloadServlet.class);
        handler.addServlet(fileDownloadHolder, "/file/download");

        ServletHolder searchHolder = new ServletHolder("Search", SearchServlet.class);
        handler.addServlet(searchHolder, "/search");

        ServletHolder downloadHolder = new ServletHolder("Download", DownloadServlet.class);
        handler.addServlet(downloadHolder, "/download");

        handler.setBaseResource(ResourceFactory.of(handler).newResource(WebServer.class.getClassLoader().getResource("static")));
        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        staticHolder.setInitParameter("dirAllowed", "false"); // 禁止目录浏览
        handler.addServlet(staticHolder, "/");


        JakartaWebSocketServletContainerInitializer.configure(handler, (servletContext, container) ->
        {
            // Configure the ServerContainer.
            container.setDefaultMaxTextMessageBufferSize(128 * 1024);

            // Simple registration of your WebSocket endpoints.
            container.addEndpoint(MyJavaxWebSocketEndPoint.class);

            // Advanced registration of your WebSocket endpoints.
            container.addEndpoint(
                    ServerEndpointConfig.Builder.create(MyJavaxWebSocketEndPoint.class, "/ws")
                            .subprotocols(List.of("ws"))
                            .build()
            );
        });

        server.setHandler(handler);
        Console.print("Server start on port {} \n", port);
        new Thread(() -> {
            try {
                server.start();
                server.join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

    }
}
