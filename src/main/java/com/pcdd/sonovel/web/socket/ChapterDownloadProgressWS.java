package com.pcdd.sonovel.web.socket;

import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.web.util.MessageUtils;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/book/progress")
public class ChapterDownloadProgressWS {

    @OnOpen
    public void onOpen(Session session) {
        MessageUtils.putSession(session);
        Console.log("WebSocket 连接已打开，session ID: {}", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        MessageUtils.removeSession(session);
        Console.log("WebSocket 连接已关闭，session ID: {}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Console.error("WebSocket 连接发生错误，session ID: {}，错误信息: {}", session.getId(), throwable.getMessage(), throwable);
    }

}