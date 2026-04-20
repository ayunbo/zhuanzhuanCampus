package com.zhuanzhuan.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOrderUpdateDTO {
    private Long id;
    private String meetLocation;
    private LocalDateTime meetTime;
    private String remark;
}