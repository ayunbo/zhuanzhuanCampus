package com.zhuanzhuan.vo.chat;

import lombok.Data;

@Data
public class ChatSessionCreateSupportVO {

    /**
     * 创建会话时，从商品表补齐出的商品快照。
     */
    private Long goodsId;
    private Long sellerId;
    private String goodsTitle;
    private String goodsCover;
    private Integer goodsStatus;
}
