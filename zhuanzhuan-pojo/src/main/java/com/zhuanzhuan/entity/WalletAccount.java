package com.zhuanzhuan.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletAccount implements Serializable {

    private Long id;

    private String walletUserNo;

    private String loginName;

    private String walletName;

    private String phone;

    private String payPassword;

    private BigDecimal balance;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
