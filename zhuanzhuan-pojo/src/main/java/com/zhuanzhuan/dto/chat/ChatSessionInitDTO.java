package com.zhuanzhuan.dto.chat;

import lombok.Data;

@Data
public class ChatSessionInitDTO {

    /**
     * 商品 id。
     */
    private Long goodsId;

    /**
     * 买家 id。
     */
    private Long buyerId;

    /**
     * 卖家 id。
     */
    private Long sellerId;
}
