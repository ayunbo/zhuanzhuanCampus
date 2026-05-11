package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletOverviewVO implements Serializable {

    private String loginName;

    private String walletUserNo;

    private String walletName;

    private BigDecimal walletBalance;

    private Integer walletStatus;

    private List<WalletBankCardVO> bankCards;
}
