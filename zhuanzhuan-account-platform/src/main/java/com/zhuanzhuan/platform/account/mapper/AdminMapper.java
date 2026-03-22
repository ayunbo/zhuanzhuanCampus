package com.zhuanzhuan.platform.account.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.AdminPageQueryDTO;
import com.zhuanzhuan.entity.Admin;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.AdminVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员数据访问接口
 */
@Mapper
public interface AdminMapper {

    /**
     * 按账号查询管理员（登录使用）
     */
    Admin getByUsername(String username);

    /**
     * 按主键查询管理员
     */
    Admin getById(Long id);

    /**
     * 分页查询管理员
     */
    List<AdminVO> pageQuery(AdminPageQueryDTO pageQueryDTO);

    /**
     * 新增管理员
     */
    @AutoFill(OperationType.INSERT)
    int insert(Admin admin);

    /**
     * 按 ID 动态更新管理员
     */
    @AutoFill(OperationType.UPDATE)
    int updateByIdSelective(Admin admin);

    /**
     * 按 ID 删除管理员
     */
    int deleteById(Long id);

    /**
     * 统计同账号（排除指定 ID）
     */
    int countByUsernameExcludeId(@Param("username") String username, @Param("id") Long id);

    /**
     * 统计指定状态的管理员数量
     */
    int countByStatus(@Param("status") Integer status);
}
