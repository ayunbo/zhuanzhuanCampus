package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewSubmitDTO implements Serializable {

    private Long orderId;
    private Integer score;
    private String content;
    private String images;
    private Integer anonymous;
}
