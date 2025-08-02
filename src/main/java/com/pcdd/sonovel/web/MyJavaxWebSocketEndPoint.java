package com.pcdd.sonovel.web;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws")
public class MyJavaxWebSocketEndPoint {

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        MessageUtil.putSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        MessageUtil.removeSession(session);
    }
}
