package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 管理端商品审核请求对象。
 */
@Data
public class AdminGoodsAuditDTO {

    /**
     * 审核后的商品状态。
     */
    private Integer status;

    /**
     * 驳回原因。
     */
    private String reason;
}
