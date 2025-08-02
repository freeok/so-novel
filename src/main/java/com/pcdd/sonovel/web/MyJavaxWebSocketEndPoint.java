package com.pcdd.sonovel.web;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws")
public class MyJavaxWebSocketEndPoint {

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        try {
            MessageUtil.putSession(session);
            MessageUtil.pushInit(session);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        SequenceInputStreamUtil.inputMessage(message);
        MessageUtil.pushCommand(message);
    }

    @OnClose
    public void onClose(Session session) {
        MessageUtil.removeSession(session);
    }
}
