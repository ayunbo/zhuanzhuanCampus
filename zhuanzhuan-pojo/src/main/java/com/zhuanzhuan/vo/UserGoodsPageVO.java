package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户端商品分页返回对象。
 */
@Data
public class UserGoodsPageVO {

    private Long id;
    private Long sellerId;
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
    private Integer viewCount;
    private Integer favoriteCount;
    private String sellerName;
    private String sellerAvatar;
    private LocalDateTime publishTime;
}
