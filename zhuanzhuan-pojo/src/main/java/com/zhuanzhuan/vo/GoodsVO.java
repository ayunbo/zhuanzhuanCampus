package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsVO {

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
    private String statusDesc;
    private String cover;
    private String reason;
    private Long auditAdminId;
    private LocalDateTime auditTime;
    private LocalDateTime publishTime;
    private Integer viewCount;
    private Integer favoriteCount;
    private Long lockOrderId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
