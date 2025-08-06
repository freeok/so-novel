package com.pcdd.sonovel.web;

import jakarta.websocket.Session;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageUtil {
    static Map<String, Session> sessions = new ConcurrentHashMap<>();

    public static void putSession(Session session) {
        sessions.put(session.getId(), session);
    }
    public static void removeSession(Session session) {
        sessions.remove(session.getId());
    }
    public static void pushMessageToAll(String message) {
        if(sessions.isEmpty()){
            return;
        }
        try {
            for (Session session : sessions.values()) {
                sendMessage(session, message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }
}
