package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PayStatusVO implements Serializable {

    private Long orderId;

    private Long payId;

    private String payNo;

    private String requestNo;

    private Integer method;

    private Integer payStatus;

    private Integer orderStatus;

    private LocalDateTime expireTime;

    private LocalDateTime payTime;
}
