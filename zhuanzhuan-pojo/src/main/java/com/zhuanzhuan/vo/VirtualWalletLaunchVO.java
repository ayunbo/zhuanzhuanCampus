package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VirtualWalletLaunchVO implements Serializable {

    private Long orderId;

    private Long payId;

    private String payNo;

    private String requestNo;

    private BigDecimal amount;

    private Integer method;

    private Integer payStatus;

    private LocalDateTime expireTime;

    private String callbackUrl;

    private String returnUrl;

    private String walletScheme;
}
