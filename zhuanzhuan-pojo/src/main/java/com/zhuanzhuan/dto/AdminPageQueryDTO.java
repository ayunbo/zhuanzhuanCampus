package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class AdminPageQueryDTO {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 管理员名称（模糊）
     */
    private String name;

    /**
     * 管理员账号（模糊）
     */
    private String username;

    /**
     * 手机号（模糊）
     */
    private String phone;

    /**
     * 状态（精确）
     */
    private Integer status;
}
