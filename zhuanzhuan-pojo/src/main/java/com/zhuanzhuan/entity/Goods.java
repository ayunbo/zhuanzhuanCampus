package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goods {

    public static final Integer STATUS_DRAFT = 0;
    public static final Integer STATUS_PENDING_AUDIT = 1;
    public static final Integer STATUS_REJECTED = 2;
    public static final Integer STATUS_ON_SALE = 3;
    public static final Integer STATUS_LOCKED = 4;
    public static final Integer STATUS_SOLD = 5;
    public static final Integer STATUS_OFF_SHELF = 6;

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
    private String reason;
    private Long auditAdminId;
    private LocalDateTime auditTime;
    private LocalDateTime publishTime;
    private Integer viewCount;
    private Integer favoriteCount;
    private Long lockOrderId;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
