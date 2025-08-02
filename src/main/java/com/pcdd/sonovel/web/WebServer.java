package com.pcdd.sonovel.web;

import cn.hutool.core.lang.Console;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.io.IOException;
import java.util.List;

public class WebServer {
    Server server;

    public void init() {
        SequenceInputStreamUtil.repaceInputStream();
        ConsoleOutputInterceptor.addListener(line -> {
                try {
                    MessageUtil.pushMessageToAll(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        );
        int port = 7765;
        server = new Server(port);


        ServletContextHandler handler = new ServletContextHandler("/");

        handler.setBaseResource(ResourceFactory.of(handler).newResource(WebServer.class.getClassLoader().getResource("static")));
        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        staticHolder.setInitParameter("dirAllowed", "false"); // 禁止目录浏览
        staticHolder.setInitParameter("precompressed", "gzip=.gz"); // 支持预压缩资源
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
