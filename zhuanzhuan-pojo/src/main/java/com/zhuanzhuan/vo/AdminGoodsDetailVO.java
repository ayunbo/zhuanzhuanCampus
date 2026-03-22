package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端商品详情返回对象。
 */
@Data
public class AdminGoodsDetailVO {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private String sellerStudentNo;
    private String sellerAvatar;
    private String sellerCampus;
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
    private Long auditAdminId;
    private LocalDateTime auditTime;
    private LocalDateTime publishTime;
    private Integer viewCount;
    private Integer favoriteCount;
    private Long lockOrderId;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<GoodsImageVO> images;
}
