package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class GoodsStatUpdateDTO {

    private Long goodsId;
    private Integer viewDelta;
    private Integer favoriteDelta;
}
