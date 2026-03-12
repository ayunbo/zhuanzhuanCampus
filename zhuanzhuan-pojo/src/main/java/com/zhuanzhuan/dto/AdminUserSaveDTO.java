package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class AdminUserSaveDTO {

    private Long id;
    private String studentNo;
    private String password;
    private String name;
    private String phone;
    private String avatar;
    private String campus;
    private String intro;
    private Integer role;
    private Integer status;
}
