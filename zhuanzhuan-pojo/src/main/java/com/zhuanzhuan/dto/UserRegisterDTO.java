package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {

    /**
     * 学号，作为系统登录主账号
     */
    private String studentNo;

    /**
     * 明文密码，服务端接收后进行加密
     */
    private String password;

    /**
     * 昵称，允许为空
     */
    private String name;

    /**
     * 手机号，允许为空；绑定后可用于登录
     */
    private String phone;
}
