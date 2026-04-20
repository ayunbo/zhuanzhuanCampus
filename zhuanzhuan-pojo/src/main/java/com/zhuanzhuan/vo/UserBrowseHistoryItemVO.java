package com.zhuanzhuan.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户浏览历史条目
 */
@Data
public class UserBrowseHistoryItemVO {

    private Long historyId;
    private Long goodsId;
    private String title;
    private String cover;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private Integer quality;
    private String location;
    private Integer status;
    private String statusDesc;
    private Integer browseCount;
    private LocalDateTime browseTime;
}
