package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 卖家端商品分页返回对象。
 */
@Data
public class SellerGoodsPageVO {

    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String categoryName;
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
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime publishTime;
    private LocalDateTime updateTime;
}
