package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class CategorySaveDTO {

    /**
     * 分类名称（新增时必填）
     */
    private String name;

    /**
     * 父分类ID，根分类传 0 或不传
     */
    private Long parentId;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 状态：1 启用，0 禁用
     */
    private Integer status;
}
