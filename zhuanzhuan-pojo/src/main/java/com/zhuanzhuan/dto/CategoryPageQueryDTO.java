package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class CategoryPageQueryDTO {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 分类名称（模糊查询）
     */
    private String name;

    /**
     * 父分类ID（可选）
     */
    private Long parentId;

    /**
     * 状态（可选）
     */
    private Integer status;
}
