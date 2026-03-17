package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class CategoryStatusDTO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 状态：1 启用，0 禁用
     */
    private Integer status;
}
