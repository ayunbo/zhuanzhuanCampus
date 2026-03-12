package com.zhuanzhuan.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private Long id;
    /**
     * 管理员账号字段，用户登录时可为空
     */
    private String username;
    /**
     * 用户学号字段，管理员登录时可为空
     */
    private String studentNo;
    private String name;
    private Integer role;
    private String token;
}
