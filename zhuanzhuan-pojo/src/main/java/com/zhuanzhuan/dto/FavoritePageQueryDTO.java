package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 收藏列表分页查询参数
 */
@Data
public class FavoritePageQueryDTO {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 排序方向：desc(默认)、asc
     */
    private String sortType = "desc";

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public Integer getPageSize() {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    public String getSortType() {
        return "asc".equalsIgnoreCase(sortType) ? "asc" : "desc";
    }
}
