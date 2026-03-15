package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PayCreateDTO implements Serializable {

    private Long orderId;

    /**
     * 1 = mock
     * 2 = virtual wallet
     */
    private Integer payMethod;

    /**
     * web / android / h5
     */
    private String clientType;

    /**
     * Optional client return url
     */
    private String returnUrl;
}
