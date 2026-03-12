package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetailVO implements Serializable {

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
}