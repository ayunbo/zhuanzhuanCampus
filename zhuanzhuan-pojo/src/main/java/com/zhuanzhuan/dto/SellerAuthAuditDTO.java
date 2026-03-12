package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class SellerAuthAuditDTO {

    private Long authId;
    private Integer status;
    private String reason;
}
