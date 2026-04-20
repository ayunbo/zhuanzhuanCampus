package com.zhuanzhuan.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Review implements Serializable {

    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long userId;
    private Long targetUserId;
    private Integer score;
    private String content;
    private String images;
    private Integer anonymous;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
