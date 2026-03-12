package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 角色：普通用户
     */
    public static final Integer ROLE_NORMAL = 1;

    /**
     * 角色：卖家
     */
    public static final Integer ROLE_SELLER = 2;

    /**
     * 状态：正常
     */
    public static final Integer STATUS_NORMAL = 1;

    /**
     * 状态：封禁
     */
    public static final Integer STATUS_BANNED = 2;

    private Long id;
    /**
     * 学号（系统主账号）
     */
    private String studentNo;
    private String password;
    private String name;
    private String phone;
    private String avatar;
    private Integer role;
    private Integer status;
    private String campus;
    private String intro;
    private BigDecimal scoreAvg;
    private Integer reviewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
