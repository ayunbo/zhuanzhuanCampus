package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OrderSubmitDTO implements Serializable {

    private Long goodsId;
    private String meetLocation;
    private LocalDateTime meetTime;
    private String remark;
}