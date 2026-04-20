package com.zhuanzhuan.platform.account.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.AdminUserPageQueryDTO;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.UserProfileVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表数据访问接口。
 */
@Mapper
public interface UserMapper {

    /**
     * 根据学号查询用户。
     */
    User getByStudentNo(String studentNo);

    /**
     * 根据手机号查询用户。
     */
    User getByPhone(String phone);

    /**
     * 根据学号或手机号查询用户。
     */
    User getByStudentNoOrPhone(String account);

    /**
     * 根据用户 ID 查询用户。
     */
    User getById(Long id);

    /**
     * 按学号注册新用户。
     */
    int insert(User user);

    /**
     * 管理员分页查询用户
     */
    List<UserProfileVO> pageQueryAdmin(AdminUserPageQueryDTO pageQueryDTO);

    /**
     * 统计手机号是否被其他用户占用。
     */
    int countByPhoneExcludeId(@Param("phone") String phone, @Param("id") Long id);

    /**
     * 根据 ID 动态更新用户资料。
     */
    @AutoFill(OperationType.UPDATE)
    int updateByIdSelective(User user);

    /**
     * 根据 ID 删除用户。
     */
    int deleteById(Long id);

    /**
     * 将用户角色更新为卖家。
     */
    int updateRoleById(@Param("id") Long id, @Param("role") Integer role);

    /**
     * 更新卖家评分与评价次数。
     */
    int increaseReviewStats(@Param("id") Long id,
                            @Param("score") Integer score,
                            @Param("updateUser") Long updateUser);
}
