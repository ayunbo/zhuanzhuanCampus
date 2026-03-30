package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewStatsVO implements Serializable {

    private Long totalCount = 0L;
    private Long goodCount = 0L;
    private Long neutralCount = 0L;
    private Long badCount = 0L;

    private Long score5Count = 0L;
    private Long score4Count = 0L;
    private Long score3Count = 0L;
    private Long score2Count = 0L;
    private Long score1Count = 0L;
}
