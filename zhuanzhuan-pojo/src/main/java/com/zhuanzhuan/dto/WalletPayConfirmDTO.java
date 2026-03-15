package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WalletPayConfirmDTO implements Serializable {

    private Long orderId;

    private String requestNo;

    /**
     * 1 = wallet balance
     * 2 = bank card
     */
    private Integer payChannel;

    private Long bankCardId;

    private String loginName;

    private String payPassword;
}
