package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WalletAccountOpenDTO implements Serializable {

    private String walletName;

    private String phone;

    private String payPassword;

    private BigDecimal balance;
}
