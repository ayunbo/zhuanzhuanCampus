package com.zhuanzhuan.service.user.admin;

import com.zhuanzhuan.dto.AdminUserPageQueryDTO;
import com.zhuanzhuan.dto.AdminUserSaveDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.UserProfileVO;

public interface AdminUserService {

    void createUser(AdminUserSaveDTO saveDTO);

    void updateUser(AdminUserSaveDTO saveDTO);

    void deleteUser(Long id);

    UserProfileVO getUserById(Long id);

    PageResult pageQueryUser(AdminUserPageQueryDTO pageQueryDTO);
}
