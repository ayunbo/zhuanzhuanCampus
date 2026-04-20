package com.zhuanzhuan.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Pay implements Serializable {

    private Long id;
    private String payNo;
    private String requestNo;
    private Long orderId;

    private BigDecimal amount;
    private Integer method;
    private Integer status;
    private LocalDateTime payTime;

    private Integer version;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}