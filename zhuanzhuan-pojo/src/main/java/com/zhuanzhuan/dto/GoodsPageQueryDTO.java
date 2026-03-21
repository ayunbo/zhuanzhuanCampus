package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class GoodsPageQueryDTO {

    private Integer page;
    private Integer pageSize;
    private Long categoryId;
    private Long sellerId;
    private String title;
}
