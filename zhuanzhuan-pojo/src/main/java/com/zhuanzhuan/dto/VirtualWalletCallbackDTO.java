package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class VirtualWalletCallbackDTO implements Serializable {

    private String requestNo;

    private Long orderId;

    private BigDecimal amount;

    private Integer payStatus;

    private String transactionNo;

    private String walletUserId;

    private String walletAccount;

    private String bankCardMask;

    private String message;

    private Long timestamp;

    private String sign;
}
