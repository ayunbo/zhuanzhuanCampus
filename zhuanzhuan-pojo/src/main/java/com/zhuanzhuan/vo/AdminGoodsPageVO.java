package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端商品分页返回对象。
 */
@Data
public class AdminGoodsPageVO {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private String sellerStudentNo;
    private Long categoryId;
    private String categoryName;
    private String title;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private Integer quality;
    private String location;
    private Integer status;
    private String statusDesc;
    private String cover;
    private String reason;
    private LocalDateTime auditTime;
    private LocalDateTime publishTime;
    private LocalDateTime updateTime;
}
