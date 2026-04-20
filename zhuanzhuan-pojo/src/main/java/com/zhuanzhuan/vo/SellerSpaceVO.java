package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 卖家空间信息返回对象。
 */
@Data
public class SellerSpaceVO {

    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private String sellerCampus;
    private String sellerIntro;
    private BigDecimal sellerScoreAvg;
    private Integer sellerReviewCount;
    private Long onSaleCount;
    private Long soldCount;
}
