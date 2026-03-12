package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAuth {

    /**
     * 状态：待审核
     */
    public static final Integer STATUS_PENDING = 0;

    /**
     * 状态：审核通过
     */
    public static final Integer STATUS_APPROVED = 1;

    /**
     * 状态：驳回
     */
    public static final Integer STATUS_REJECTED = 2;

    /**
     * 状态：撤回
     */
    public static final Integer STATUS_REVOKED = 3;

    private Long id;
    private Long userId;
    private String realName;
    private String studentNo;
    private String phone;
    private String material;
    private Integer status;
    private String reason;
    private Long auditAdminId;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
