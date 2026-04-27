package com.zhuanzhuan.entity.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 文本消息。
     */
    public static final Integer TYPE_TEXT = 1;

    /**
     * 图片消息，数据库预留，一期未启用。
     */
    public static final Integer TYPE_IMAGE = 2;

    /**
     * 系统消息，数据库预留，一期未启用。
     */
    public static final Integer TYPE_SYSTEM = 3;

    /**
     * 未读。
     */
    public static final Integer READ_UNREAD = 0;

    /**
     * 已读。
     */
    public static final Integer READ_READ = 1;

    /**
     * chatmessage 表实体。
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
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
