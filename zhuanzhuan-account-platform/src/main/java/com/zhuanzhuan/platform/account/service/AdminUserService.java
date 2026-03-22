package com.zhuanzhuan.platform.account.service;

import com.zhuanzhuan.dto.AdminUserPageQueryDTO;
import com.zhuanzhuan.dto.AdminUserSaveDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.UserProfileVO;

/**
 * 后台用户管理服务接口。
 */
public interface AdminUserService {

    /**
     * 新增用户。
     */
    void createUser(AdminUserSaveDTO saveDTO);

    /**
     * 修改用户。
     */
    void updateUser(AdminUserSaveDTO saveDTO);

    /**
     * 删除用户。
     */
    void deleteUser(Long id);

    /**
     * 查询用户详情。
     */
    UserProfileVO getUserById(Long id);

    /**
     * 分页查询用户。
     */
    PageResult pageQueryUser(AdminUserPageQueryDTO pageQueryDTO);
}
