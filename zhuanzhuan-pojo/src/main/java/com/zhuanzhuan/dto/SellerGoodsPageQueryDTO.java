package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class SellerGoodsPageQueryDTO {

    private Integer page;
    private Integer pageSize;
    private Integer status;
}
