package com.zhuanzhuan.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletBankCard implements Serializable {

    private Long id;

    private Long walletAccountId;

    private String bankName;

    private String cardHolder;

    private String cardNo;

    private String cardNoMask;

    private Integer cardType;

    private BigDecimal balance;

    private Integer isDefault;

    private Integer status;

    private LocalDateTime bindTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
