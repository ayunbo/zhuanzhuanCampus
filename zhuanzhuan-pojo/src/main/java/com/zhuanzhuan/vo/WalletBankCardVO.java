package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WalletBankCardVO implements Serializable {

    private Long id;

    private String bankName;

    private String cardNoMask;

    private BigDecimal balance;

    private Integer isDefault;
}
