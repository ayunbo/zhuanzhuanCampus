package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class SellerReviewPageVO implements Serializable {

    private Long total = 0L;
    private List<ReviewVO> records = Collections.emptyList();
    private ReviewStatsVO stats = new ReviewStatsVO();
}
