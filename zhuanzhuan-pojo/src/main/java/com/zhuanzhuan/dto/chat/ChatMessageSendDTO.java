package com.zhuanzhuan.dto.chat;

import lombok.Data;

@Data
public class ChatMessageSendDTO {

    /**
     * 会话 id。
     */
    private Long sessionId;

    /**
     * 当前一期只支持文本内容。
     */
    private String content;
}
