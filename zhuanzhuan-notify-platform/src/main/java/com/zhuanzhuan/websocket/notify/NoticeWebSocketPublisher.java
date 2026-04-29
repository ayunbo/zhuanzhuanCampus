package com.zhuanzhuan.websocket.notify;

import com.zhuanzhuan.vo.chat.ChatPushMessageVO;
import com.zhuanzhuan.websocket.chat.ChatWebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NoticeWebSocketPublisher {

    @Autowired
    private ChatWebSocketPublisher chatWebSocketPublisher;

    public void sendToUser(Long userId, String event, Object data) {
        chatWebSocketPublisher.sendToUser(userId, new ChatPushMessageVO<>(event, data));
    }
}
