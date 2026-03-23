package com.zhuanzhuan.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端商品分页查询对象。
 */
@Data
public class AdminGoodsPageQueryDTO {

    /**
     * 页码，默认值为 1。
     */
    private Integer page = 1;

    /**
     * 每页条数，默认值为 10。
     */
    private Integer pageSize = 10;

    /**
     * 关键字。
     */
    private String keyword;

    /**
     * 商品状态。
     */
    private Integer status;

    /**
     * 卖家 ID。
     */
    private Long sellerId;

    /**
     * 分类 ID。
     */
    private Long categoryId;

    /**
     * 最低价格。
     */
    private BigDecimal minPrice;

    /**
     * 最高价格。
     */
    private BigDecimal maxPrice;

    /**
     * 获取页码。
     *
     * @return 页码
     */
    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    /**
     * 获取每页条数。
     *
     * @return 每页条数
     */
    public Integer getPageSize() {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }
}
