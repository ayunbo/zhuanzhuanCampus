package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminOrderDetailVO implements Serializable {

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
    private LocalDateTime createTime;

    // 快照
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

    // 支付信息
    private Long payId;
    private String payNo;
    private Integer payStatus;
    private Integer payMethod;
    private LocalDateTime payRecordTime;

    // 支付记录
    private List<PayRecordVO> payRecords;
}