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
public class ChatSession {

    /**
     * 当前用户在会话中的身份：卖家。
     */
    public static final Integer ROLE_SELLER = 1;

    /**
     * 当前用户在会话中的身份：买家。
     */
    public static final Integer ROLE_BUYER = 2;

    /**
     * chatsession 表实体。
     * 一个会话由 goodsId + sellerId + buyerId 唯一确定。
     */
    private Long id;
    private Long goodsId;
    private Long sellerId;
    private Long buyerId;
    private String goodsTitle;
    private String goodsCover;
    private String lastMsg;
    private LocalDateTime lastTime;
    private Integer sellerUnread;
    private Integer buyerUnread;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
