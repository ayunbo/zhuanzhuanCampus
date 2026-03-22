package com.zhuanzhuan.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PayCreateVO implements Serializable {

    private Long orderId;

    private Integer payMethod;

    private Integer payStatus;

    /**
     * direct_success / open_wallet / redirect_page
     */
    private String action;

    private String message;

    private String requestNo;

    private String walletScheme;

    private String redirectUrl;
}
