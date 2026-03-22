package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端商品详情返回对象。
 */
@Data
public class UserGoodsDetailVO {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private String sellerCampus;
    private BigDecimal sellerScoreAvg;
    private Integer sellerReviewCount;
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
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<GoodsImageVO> images;
}
