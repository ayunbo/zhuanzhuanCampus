package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class AdminUserPageQueryDTO {

    private Integer page = 1;
    private Integer pageSize = 10;
    private String studentNo;
    private String name;
    private String phone;
    private Integer role;
    private Integer status;
}
