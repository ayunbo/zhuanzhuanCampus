package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class AdminGoodsAuditDTO {

    private Long goodsId;
    /**
     * 3=approve and on sale, 2=reject
     */
    private Integer status;
    private String reason;
}
