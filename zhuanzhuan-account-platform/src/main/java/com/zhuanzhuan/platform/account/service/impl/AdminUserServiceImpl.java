package com.zhuanzhuan.platform.account.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.RoleConstant;
import com.zhuanzhuan.constant.SellerAuthStatusConstant;
import com.zhuanzhuan.constant.UserStatusConstant;
import com.zhuanzhuan.dto.AdminUserPageQueryDTO;
import com.zhuanzhuan.dto.AdminUserSaveDTO;
import com.zhuanzhuan.entity.SellerAuth;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.account.mapper.SellerAuthMapper;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.account.service.AdminUserService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.RiskControlService;
import com.zhuanzhuan.vo.UserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台用户业务实现。
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SellerAuthMapper sellerAuthMapper;

    @Autowired
    private RiskControlService riskControlService;

    /**
     * 新增用户。
     *
     * @param saveDTO 用户新增信息
     */
    @Override
    public void createUser(AdminUserSaveDTO saveDTO) {
        // 1、校验学号唯一性，避免后台创建出重复账号
        User sameStudentNoUser = userMapper.getByStudentNo(saveDTO.getStudentNo());
        if (sameStudentNoUser != null) {
            throw new BaseException(MessageConstant.STUDENT_NO_ALREADY_EXISTS);
        }

        // 2、若填写了手机号，校验手机号唯一性
        if (StringUtils.hasText(saveDTO.getPhone())) {
            User samePhoneUser = userMapper.getByPhone(saveDTO.getPhone());
            if (samePhoneUser != null) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        // 3、将 DTO 属性拷贝到用户实体
        User user = new User();
        BeanUtils.copyProperties(saveDTO, user);

        // 4、密码统一 MD5 加密后存储
        user.setPassword(DigestUtils.md5DigestAsHex(saveDTO.getPassword().getBytes()));

        // 5、未填写姓名时以学号作为显示名
        if (!StringUtils.hasText(saveDTO.getName())) {
            user.setName(saveDTO.getStudentNo());
        }

        // 6、角色和状态未指定时使用默认值
        if (user.getRole() == null) {
            user.setRole(RoleConstant.NORMAL_USER);
        }
        if (user.getStatus() == null) {
            user.setStatus(UserStatusConstant.NORMAL);
        }

        // 7、执行数据库插入
        userMapper.insert(user);
    }

    /**
     * 修改用户。
     *
     * @param saveDTO 用户修改信息（需携带 id）
     */
    @Override
    public void updateUser(AdminUserSaveDTO saveDTO) {
        // 1、查询目标用户，确认记录存在
        User existingUser = userMapper.getById(saveDTO.getId());
        if (existingUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 2、如果修改了学号，校验新学号是否与其他账号冲突
        if (StringUtils.hasText(saveDTO.getStudentNo())
                && !saveDTO.getStudentNo().equals(existingUser.getStudentNo())) {
            User sameStudentNoUser = userMapper.getByStudentNo(saveDTO.getStudentNo());
            if (sameStudentNoUser != null) {
                throw new BaseException(MessageConstant.STUDENT_NO_ALREADY_EXISTS);
            }
        }

        // 3、如果修改了手机号，校验新手机号是否与其他账号冲突
        if (StringUtils.hasText(saveDTO.getPhone())) {
            int phoneCount = userMapper.countByPhoneExcludeId(saveDTO.getPhone(), saveDTO.getId());
            if (phoneCount > 0) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        // 4、将 DTO 属性拷贝到更新实体
        User updateEntity = new User();
        BeanUtils.copyProperties(saveDTO, updateEntity);

        // 5、如果本次携带了新密码，则更新为新密码的 MD5 值；否则不修改密码字段
        if (StringUtils.hasText(saveDTO.getPassword())) {
            updateEntity.setPassword(DigestUtils.md5DigestAsHex(saveDTO.getPassword().getBytes()));
        } else {
            updateEntity.setPassword(null);
        }

        // 6、执行选择性更新（只更新非 null 字段）
        userMapper.updateByIdSelective(updateEntity);
        riskControlService.evictAuthStatus("user", saveDTO.getId());
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     */
    @Override
    public void deleteUser(Long id) {
        // 1、查询目标用户，确认记录存在
        User existingUser = userMapper.getById(id);
        if (existingUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 2、存在待审核的卖家申请时不允许删除，避免审核数据悬空
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(id);
        if (latestAuth != null && SellerAuthStatusConstant.PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_PENDING_AUTH);
        }

        // 3、执行物理删除
        userMapper.deleteById(id);
        riskControlService.evictAuthStatus("user", id);
    }

    /**
     * 根据 ID 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户资料视图对象
     */
    @Override
    public UserProfileVO getUserById(Long id) {
        // 1、查询用户，不存在则抛出业务异常
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 2、将实体属性拷贝到 VO 并返回
        UserProfileVO userProfileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, userProfileVO);
        return userProfileVO;
    }

    /**
     * 分页查询用户列表。
     *
     * @param pageQueryDTO 分页查询条件（page、pageSize 已有默认值）
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryUser(AdminUserPageQueryDTO pageQueryDTO) {
        // 1、启动分页插件，page 和 pageSize 由 DTO 默认值保证非空
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 2、执行查询，PageHelper 自动拦截并追加 LIMIT
        List<UserProfileVO> records = userMapper.pageQueryAdmin(pageQueryDTO);
        Page<UserProfileVO> pageInfo = (Page<UserProfileVO>) records;

        // 3、封装总记录数和当前页数据返回
        return new PageResult(pageInfo.getTotal(), records);
    }
}
