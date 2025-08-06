package com.pcdd.sonovel.web.socket;

import com.pcdd.sonovel.web.util.MessageUtils;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/book/progress")
public class ChapterDownloadProgressWS {

    @OnOpen
    public void onOpen(Session session) {
        MessageUtils.putSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        MessageUtils.removeSession(session);
    }

}