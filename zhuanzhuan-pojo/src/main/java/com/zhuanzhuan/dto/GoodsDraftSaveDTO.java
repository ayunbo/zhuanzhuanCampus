package com.zhuanzhuan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsDraftSaveDTO {

    private Long categoryId;
    private String title;
    private String detail;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private Integer quality;
    private String location;
    private String cover;
}
