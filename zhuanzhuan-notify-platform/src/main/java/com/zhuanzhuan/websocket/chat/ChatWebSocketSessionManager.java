package com.zhuanzhuan.websocket.chat;

import jakarta.websocket.Session;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketSessionManager {

    /**
     * 一个用户可能同时开多个页面或多个设备，所以这里保存 userId -> Session 集合。
     */
    private static final ConcurrentHashMap<Long, Set<Session>> USER_SESSION_MAP = new ConcurrentHashMap<>();

    public void addSession(Long userId, Session session) {
        USER_SESSION_MAP.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(Long userId, Session session) {
        Set<Session> sessions = USER_SESSION_MAP.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            USER_SESSION_MAP.remove(userId);
        }
    }

    public void sendToUser(Long userId, String payload) {
        Set<Session> sessions = USER_SESSION_MAP.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        // 同一用户的所有在线连接都推送，保证多端状态一致。
        for (Session session : sessions) {
            if (session != null && session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(payload);
                } catch (Exception ex) {
                    try {
                        session.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
}
