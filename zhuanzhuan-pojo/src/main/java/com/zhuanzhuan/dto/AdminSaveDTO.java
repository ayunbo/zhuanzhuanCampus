package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class AdminSaveDTO {

    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private Integer status;
}
