package com.zhuanzhuan.websocket.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zhuanzhuan.json.JacksonObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatWebSocketPublisher {

    /**
     * 统一把推送对象序列化成 JSON 后发送给前端。
     */
    private static final JacksonObjectMapper OBJECT_MAPPER = new JacksonObjectMapper();

    @Autowired
    private ChatWebSocketSessionManager chatWebSocketSessionManager;

    public void sendToUser(Long userId, Object message) {
        if (userId == null || message == null) {
            return;
        }
        try {
            chatWebSocketSessionManager.sendToUser(userId, OBJECT_MAPPER.writeValueAsString(message));
        } catch (JsonProcessingException ignored) {
            // 推送失败不影响主业务流程，避免因为 WebSocket 问题影响消息发送事务。
        }
    }
}
