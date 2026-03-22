package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;
//订单分页查询dto
@Data
public class OrderPageQueryDTO implements Serializable {

    private Integer page;
    private Integer pageSize;

    // 订单状态，可选
    private Integer status;

    // 1=我买到的 2=我卖出的
    private Integer type;
}