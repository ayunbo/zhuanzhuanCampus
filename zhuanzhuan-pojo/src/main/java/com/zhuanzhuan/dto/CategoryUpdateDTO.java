package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class CategoryUpdateDTO {

    /**
     * 分类ID（必填）
     */
    private Long id;

    /**
     * 分类名称（必填）
     */
    private String name;

    /**
     * 父分类ID（可选，不传则保持不变）
     */
    private Long parentId;

    /**
     * 排序值（可选，不传则保持不变）
     */
    private Integer sort;
}
