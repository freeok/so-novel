package com.pcdd.sonovel.web.util;

import cn.hutool.core.lang.Console;
import jakarta.websocket.Session;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CopyOnWriteArraySet;

@UtilityClass
public class MessageUtils {

    // 推荐使用 CopyOnWriteArraySet 来存储会话，以避免遍历时的并发修改问题
    private final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    public void putSession(Session session) {
        sessions.add(session);
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public void pushMessageToAll(String message) {
        for (Session session : sessions) {
            try {
                // 使用 getAsyncRemote().sendText() 进行异步发送，避免阻塞
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                Console.error("发送消息失败，session ID: {}，错误信息: {}", session.getId(), e.getMessage());
                // 移除失效会话
                sessions.remove(session);
            }
        }
    }

}