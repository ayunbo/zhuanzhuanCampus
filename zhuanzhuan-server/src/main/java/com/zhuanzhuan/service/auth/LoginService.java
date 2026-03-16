package com.zhuanzhuan.service.auth;

import com.zhuanzhuan.dto.AdminLoginDTO;
import com.zhuanzhuan.dto.UserLoginDTO;
import com.zhuanzhuan.vo.LoginVO;

/**
 * 登录业务服务接口
 */
public interface LoginService {

    /**
     * 管理员登录
     */
    LoginVO adminLogin(AdminLoginDTO adminLoginDTO);

    /**
     * 用户统一登录（普通用户/卖家共用）
     */
    LoginVO userLogin(UserLoginDTO userLoginDTO);
}
