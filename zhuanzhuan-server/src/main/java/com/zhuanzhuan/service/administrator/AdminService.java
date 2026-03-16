package com.zhuanzhuan.service.administrator;

import com.zhuanzhuan.dto.AdminPageQueryDTO;
import com.zhuanzhuan.dto.AdminSaveDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.AdminVO;

/**
 * 管理员管理业务服务接口。
 */
public interface AdminService {

    /**
     * 新增管理员。
     */
    void createAdmin(AdminSaveDTO adminSaveDTO);

    /**
     * 修改管理员。
     */
    void updateAdmin(AdminSaveDTO adminSaveDTO);

    /**
     * 删除管理员。
     */
    void deleteAdmin(Long id);

    /**
     * 查询单个管理员。
     */
    AdminVO getAdminById(Long id);

    /**
     * 分页查询管理员。
     */
    PageResult pageQueryAdmin(AdminPageQueryDTO pageQueryDTO);
}
