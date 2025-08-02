package com.pcdd.sonovel.web;

import jakarta.websocket.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageUtil {
    static Map<String, Session> sessions = new ConcurrentHashMap<>();
    static ArrayList<String> cacheMessage = new ArrayList<>();

    static void putSession(Session session) {
        sessions.put(session.getId(), session);
    }
    static void removeSession(Session session) {
        sessions.remove(session.getId());
    }
    static void pushMessageToAll(String message) throws IOException {
        for (Session session : sessions.values()) {
            sendMessage(session, message);
        }
        if(cacheMessage.size() > 200){
            cacheMessage.remove(0);
        }
        cacheMessage.add(message);
    }
    static void pushInit(Session session) throws IOException {
        for (String message : cacheMessage) {
            sendMessage(session, message);
        }
    }
    static void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }
    static void pushCommand(String message){
        int size = cacheMessage.size();
        if(size > 0){
            String lastMessage = cacheMessage.get(size-1);
            if(lastMessage.contains("==>")){
                cacheMessage.set(size-1,lastMessage + message);
            }
        }
    }
}
