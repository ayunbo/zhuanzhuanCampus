package com.zhuanzhuan.vo.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatReadReceiptVO {

    /**
     * 已读回执，表示当前会话中哪些消息已被对方读到。
     */
    private Long sessionId;
    private Long readerId;
    private LocalDateTime readTime;
    private List<Long> messageIds;
}
