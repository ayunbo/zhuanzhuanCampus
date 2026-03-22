package com.zhuanzhuan.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSnapshot implements Serializable {

    private Long id;
    private Long orderId;

    private String goodsTitle;
    private String goodsCover;
    private BigDecimal goodsPrice;

    private String sellerName;
    private String sellerPhone;
    private String buyerName;
    private String buyerPhone;

    private String meetLocation;
    private LocalDateTime meetTime;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}