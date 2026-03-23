package com.zhuanzhuan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品保存请求对象。
 */
@Data
public class GoodsSaveDTO {

    /**
     * 商品分类 ID。
     */
    private Long categoryId;

    /**
     * 商品标题。
     */
    private String title;

    /**
     * 商品描述。
     */
    private String detail;

    /**
     * 商品售价。
     */
    private BigDecimal price;

    /**
     * 商品原价。
     */
    private BigDecimal oldPrice;

    /**
     * 商品成色。
     */
    private Integer quality;

    /**
     * 面交地点。
     */
    private String location;

    /**
     * 商品图片地址列表。
     */
    private List<String> imageUrls;
}
