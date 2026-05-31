package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    private Long id;
    private Long userId;
    private String querySign;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
