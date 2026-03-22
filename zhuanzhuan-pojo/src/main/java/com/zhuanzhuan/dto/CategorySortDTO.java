package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class CategorySortDTO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 排序值
     */
    private Integer sort;
}
