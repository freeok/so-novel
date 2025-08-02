package com.pcdd.sonovel.web;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;


@ServerEndpoint("/ws")
public class MyJavaxWebSocketEndPoint {

    @OnOpen
    public void onOpen(Session session) {
        MessageUtil.putSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        MessageUtil.removeSession(session);
    }
}
