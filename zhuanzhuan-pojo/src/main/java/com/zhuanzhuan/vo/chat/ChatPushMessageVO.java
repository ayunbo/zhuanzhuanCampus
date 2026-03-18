package com.zhuanzhuan.vo.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatPushMessageVO<T> {

    /**
     * WebSocket 事件名，例如 chat.message / chat.unread。
     */
    private String event;

    /**
     * 当前事件对应的数据体。
     */
    private T data;
}
