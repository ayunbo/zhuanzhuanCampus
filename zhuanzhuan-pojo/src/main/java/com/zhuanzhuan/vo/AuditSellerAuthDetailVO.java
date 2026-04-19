package com.zhuanzhuan.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核日志中的卖家认证详情。
 */
@Data
public class AuditSellerAuthDetailVO {

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
    private String auditAdminName;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
