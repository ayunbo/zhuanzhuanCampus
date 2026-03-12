package com.zhuanzhuan.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Goods implements Serializable {

    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String title;
    private String detail;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private Integer quality;
    private String location;
    private Integer status;
    private String cover;
    private Long lockOrderId;
    private Integer version;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}