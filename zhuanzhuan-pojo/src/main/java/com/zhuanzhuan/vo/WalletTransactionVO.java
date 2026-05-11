package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletTransactionVO implements Serializable {

    private Long id;

    private Long orderId;

    private String recordNo;

    private String content;

    private Integer status;

    private String channelResponse;

    private BigDecimal amount;

    private Integer payMethod;

    private String orderNo;

    private LocalDateTime createTime;
}
