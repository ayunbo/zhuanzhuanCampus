package com.zhuanzhuan.dto.chat;

import lombok.Data;

@Data
public class ChatSessionReadDTO {

    /**
     * 要标记已读的会话 id。
     */
    private Long sessionId;
}
