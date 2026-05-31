package com.zhuanzhuan.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端商品分页查询对象。
 */
@Data
public class GoodsPageQueryDTO {

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
     * 兼容前端传入的 title 参数。
     */
    private String title;

    /**
     * 分类 ID。
     */
    private Long categoryId;

    /**
     * 卖家 ID。
     */
    private Long sellerId;

    /**
     * 商品状态（用户端仅支持查询在售或已售）。
     */
    private Integer status;

    /**
     * 最低价格。
     */
    private BigDecimal minPrice;

    /**
     * 最高价格。
     */
    private BigDecimal maxPrice;

    /**
     * 成色。
     */
    private Integer quality;

    /**
     * 位置 / 校区。
     */
    private String location;

    /**
     * 排序方式：time、price_asc、price_desc、hot。
     */
    private String sortBy;

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

    public String getEffectiveKeyword() {
        if (keyword != null && !keyword.isBlank()) {
            return keyword.trim();
        }
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        return null;
    }

    public String getEffectiveSortBy() {
        if (sortBy == null || sortBy.isBlank()) {
            return "time";
        }
        return sortBy.trim().toLowerCase();
    }
}
