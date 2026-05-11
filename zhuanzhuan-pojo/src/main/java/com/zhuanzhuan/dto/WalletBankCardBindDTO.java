package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WalletBankCardBindDTO implements Serializable {

    private String bankName;

    private String cardHolder;

    private String cardNo;

    private Integer cardType;

    private BigDecimal balance;

    private Integer isDefault;
}
