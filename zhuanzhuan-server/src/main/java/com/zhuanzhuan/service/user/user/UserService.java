package com.zhuanzhuan.service.user.user;

import com.zhuanzhuan.dto.UserProfileUpdateDTO;
import com.zhuanzhuan.dto.UserRegisterDTO;
import com.zhuanzhuan.vo.UserProfileVO;

/**
 * 用户资料业务服务接口。
 */
public interface UserService {

    /**
     * 用户注册。
     */
    void register(UserRegisterDTO userRegisterDTO);

    /**
     * 查询当前登录用户资料。
     */
    UserProfileVO getCurrentUserProfile();

    /**
     * 修改当前登录用户资料。
     */
    void updateCurrentUserProfile(UserProfileUpdateDTO userProfileUpdateDTO);

    /**
     * 注销当前登录用户。
     */
    void deleteCurrentUser();
}