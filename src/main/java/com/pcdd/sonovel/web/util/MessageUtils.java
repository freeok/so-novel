package com.pcdd.sonovel.web.util;

import cn.hutool.core.lang.Console;
import jakarta.websocket.Session;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class MessageUtils {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void putSession(Session session) {
        sessions.put(session.getId(), session);
    }

    public void removeSession(Session session) {
        sessions.remove(session.getId());
    }

    public void pushMessageToAll(String message) {
        for (Session session : sessions.values()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                Console.error("发送消息失败: {}", e.getMessage());
            }
        }
    }

}