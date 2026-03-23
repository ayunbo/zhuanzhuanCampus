package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ReviewVO implements Serializable {

    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long userId;
    private Long targetUserId;
    private Integer score;
    private String content;
    private String images;
    private Integer anonymous;
    private String reviewerName;
    private String reviewerAvatar;
    private LocalDateTime createTime;
}
