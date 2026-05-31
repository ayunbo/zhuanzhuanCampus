package com.zhuanzhuan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SearchHistoryRecordDTO {

    private String keyword;
    private Long categoryId;
    private Long sellerId;
    private Integer status;
    private Integer quality;
    private String location;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy;
}
