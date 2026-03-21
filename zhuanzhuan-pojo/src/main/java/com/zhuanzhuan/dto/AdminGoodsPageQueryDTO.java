package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class AdminGoodsPageQueryDTO {

    private Integer page;
    private Integer pageSize;
    private Integer status;
    private Long sellerId;
    private String title;
}
