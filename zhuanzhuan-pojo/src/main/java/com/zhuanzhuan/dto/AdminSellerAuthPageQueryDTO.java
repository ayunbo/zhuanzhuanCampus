package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 管理员卖家认证分页条件查询对象。
 */
@Data
public class AdminSellerAuthPageQueryDTO {

    /**
     * 页码（默认 1）
     */
    private Integer page = 1;

    /**
     * 每页条数（默认 10）
     */
    private Integer pageSize = 10;

    /**
     * 姓名模糊查询（匹配认证姓名或用户昵称）
     */
    private String name;

    /**
     * 手机号模糊查询
     */
    private String phone;

    /**
     * 学号模糊查询
     */
    private String studentNo;

    /**
     * 认证状态（为空时默认查待审核）
     */
    private Integer status;
}
