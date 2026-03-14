package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class AdminOrderPageQueryDTO {

    private Integer page;
    private Integer pageSize;

    /**
     * 订单状态，可选
     */
    private Integer status;

    /**
     * 订单号，可选
     */
    private String orderNo;
}