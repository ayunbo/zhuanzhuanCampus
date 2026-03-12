package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {

    private String name;
    private String phone;
    private String avatar;
    private String campus;
    private String intro;
}
