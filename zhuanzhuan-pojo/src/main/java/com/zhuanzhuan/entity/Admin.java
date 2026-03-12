package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    /**
     * 状态：正常
     */
    public static final Integer STATUS_NORMAL = 1;

    /**
     * 状态：禁用
     */
    public static final Integer STATUS_DISABLED = 2;

    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
