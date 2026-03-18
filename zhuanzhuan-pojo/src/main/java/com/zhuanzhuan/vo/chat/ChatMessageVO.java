package com.zhuanzhuan.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    /**
     * 前端消息展示对象。
     */
    private Long id;
    private Long sessionId;
    private Long senderId;
    private Long receiverId;
    private Integer type;
    private String content;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
    /**
     * 是否为当前登录用户本人发送。
     * 前端可直接用它决定左右气泡布局。
     */
    private Boolean mine;
}
