package com.zhuanzhuan.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAuthResultVO {

    private Long id;
    private Long userId;
    private String userName;
    private String realName;
    private String studentNo;
    private String phone;
    private String material;
    private Integer status;
    private String statusDesc;
    private String reason;
    private Long auditAdminId;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
