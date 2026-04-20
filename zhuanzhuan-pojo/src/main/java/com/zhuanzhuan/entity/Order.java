package com.zhuanzhuan.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order implements Serializable {

    private Long id;
    private String orderNo;
    private Long goodsId;
    private Long sellerId;
    private Long buyerId;

    private BigDecimal amount;
    private Integer status;

    private LocalDateTime expireTime;
    private LocalDateTime payTime;
    private LocalDateTime closeTime;
    private LocalDateTime completeTime;

    private Integer version;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}