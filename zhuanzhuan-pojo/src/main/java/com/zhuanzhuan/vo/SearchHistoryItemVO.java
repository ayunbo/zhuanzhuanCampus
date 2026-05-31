package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SearchHistoryItemVO {

    private Long historyId;
    private String keyword;
    private Long categoryId;
    private Long sellerId;
    private Integer status;
    private Integer quality;
    private String location;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy;
    private Integer searchCount;
    private LocalDateTime lastSearchTime;
}
