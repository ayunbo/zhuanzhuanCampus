package com.zhuanzhuan.platform.account.service.impl;

import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.RoleConstant;
import com.zhuanzhuan.constant.SellerAuthStatusConstant;
import com.zhuanzhuan.constant.UserStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.UserProfileUpdateDTO;
import com.zhuanzhuan.dto.UserRegisterDTO;
import com.zhuanzhuan.entity.SellerAuth;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.platform.account.mapper.SellerAuthMapper;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.account.service.UserService;
import com.zhuanzhuan.service.RiskControlService;
import com.zhuanzhuan.vo.UserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

/**
 * 用户业务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SellerAuthMapper sellerAuthMapper;

    @Autowired
    private RiskControlService riskControlService;

    /**
     * 用户注册。
     *
     * @param userRegisterDTO 注册信息（学号、密码、手机号等）
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        // 1、校验必填项：学号和密码不允许为空
        if (!StringUtils.hasText(userRegisterDTO.getStudentNo())) {
            throw new BaseException(MessageConstant.STUDENT_NO_EMPTY);
        }
        if (!StringUtils.hasText(userRegisterDTO.getPassword())) {
            throw new BaseException(MessageConstant.PASSWORD_EMPTY);
        }

        // 2、若填写了手机号，校验手机号唯一性，避免绑定已被使用的手机号
        if (StringUtils.hasText(userRegisterDTO.getPhone())) {
            User phoneUser = userMapper.getByPhone(userRegisterDTO.getPhone());
            if (phoneUser != null) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        // 3、校验学号唯一性，同一学号不允许重复注册
        User studentNoUser = userMapper.getByStudentNo(userRegisterDTO.getStudentNo());
        if (studentNoUser != null) {
            throw new BaseException(MessageConstant.STUDENT_NO_ALREADY_EXISTS);
        }

        // 4、将 DTO 属性拷贝到用户实体
        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);

        // 5、密码统一 MD5 加密后存储
        user.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));

        // 6、未填写姓名时以学号作为初始显示名
        if (!StringUtils.hasText(userRegisterDTO.getName())) {
            user.setName(userRegisterDTO.getStudentNo());
        }

        // 7、注册用户默认为普通用户角色、正常状态
        user.setRole(RoleConstant.NORMAL_USER);
        user.setStatus(UserStatusConstant.NORMAL);

        // 8、执行数据库插入
        userMapper.insert(user);
    }

    /**
     * 查询当前登录用户的个人资料。
     *
     * @return 用户资料视图对象
     */
    @Override
    public UserProfileVO getCurrentUserProfile() {
        // 1、获取当前登录用户 ID，未登录则拒绝
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、查询用户，账号不存在则抛出业务异常
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 3、将实体属性拷贝到 VO 并返回
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 修改当前登录用户的个人资料。
     *
     * @param userProfileUpdateDTO 要修改的资料字段（均为可选）
     */
    @Override
    public void updateCurrentUserProfile(UserProfileUpdateDTO userProfileUpdateDTO) {
        // 1、获取当前登录用户 ID，未登录则拒绝
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、查询当前用户，确认账号存在
        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 3、如果修改了手机号，校验新手机号是否已被其他账号使用
        if (StringUtils.hasText(userProfileUpdateDTO.getPhone())) {
            int phoneUsedCount = userMapper.countByPhoneExcludeId(userProfileUpdateDTO.getPhone(), userId);
            if (phoneUsedCount > 0) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        // 4、将 DTO 属性拷贝到更新实体，并绑定当前用户 ID
        User user = new User();
        BeanUtils.copyProperties(userProfileUpdateDTO, user);
        user.setId(userId);

        // 5、执行选择性更新（只更新非 null 字段）
        userMapper.updateByIdSelective(user);
        riskControlService.evictAuthStatus("user", userId);
    }

    /**
     * 注销当前登录用户账号。
     */
    @Override
    public void deleteCurrentUser() {
        // 1、获取当前登录用户 ID，未登录则拒绝
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、查询当前用户，确认账号存在
        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 3、卖家账号不允许自主注销，需联系管理员处理
        if (RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_SELLER);
        }

        // 4、存在待审核的卖家申请时不允许注销，避免审核数据悬空
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth != null && SellerAuthStatusConstant.PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_PENDING_AUTH);
        }

        // 5、执行物理删除
        userMapper.deleteById(userId);
        riskControlService.evictAuthStatus("user", userId);
    }
}
