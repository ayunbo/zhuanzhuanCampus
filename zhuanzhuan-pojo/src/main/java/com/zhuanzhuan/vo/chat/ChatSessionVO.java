package com.zhuanzhuan.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionVO {

    /**
     * 前端会话列表 / 会话详情统一返回对象。
     */
    private Long sessionId;
    private Long goodsId;
    private Long sellerId;
    private Long buyerId;
    private String goodsTitle;
    private String goodsCover;
    private String lastMsg;
    private LocalDateTime lastTime;
    /**
     * 当前登录用户在该会话中的未读数。
     */
    private Integer unreadCount;

    /**
     * 当前用户在该会话里的身份：seller / buyer。
     */
    private String myRole;

    /**
     * 聊天对方用户 id。
     */
    private Long targetUserId;
    private String targetUserName;
    private String targetUserAvatar;
}
