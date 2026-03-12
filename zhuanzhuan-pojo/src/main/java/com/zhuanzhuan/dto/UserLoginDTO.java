package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class UserLoginDTO {

    /**
     * 登录账号：学号或手机号
     */
    private String account;
    private String password;
}
