package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;
//模拟支付
@Data
public class PaySubmitDTO implements Serializable {

    private Long orderId;
    private String requestNo;
}