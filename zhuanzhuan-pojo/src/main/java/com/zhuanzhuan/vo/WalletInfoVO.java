package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletInfoVO implements Serializable {

    private Long orderId;

    private String requestNo;

    private String loginName;

    private String walletUserNo;

    private String walletName;

    private BigDecimal amount;

    private BigDecimal walletBalance;

    private List<WalletBankCardVO> bankCards;
}
