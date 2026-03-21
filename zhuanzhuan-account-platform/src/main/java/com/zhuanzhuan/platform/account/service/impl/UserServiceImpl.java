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

    /**
     * 用户注册。
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        // 1、校验注册参数
        if (userRegisterDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、整理注册字段
        String studentNo = userRegisterDTO.getStudentNo();
        if (StringUtils.hasText(studentNo)) {
            studentNo = studentNo.trim();
        } else {
            studentNo = null;
        }

        String password = userRegisterDTO.getPassword();
        if (!StringUtils.hasText(password)) {
            password = null;
        }

        String phone = userRegisterDTO.getPhone();
        if (StringUtils.hasText(phone)) {
            phone = phone.trim();
        } else {
            phone = null;
        }

        String name = userRegisterDTO.getName();
        if (!StringUtils.hasText(name)) {
            name = null;
        }

        // 3、校验基础信息
        if (!StringUtils.hasText(studentNo)) {
            throw new BaseException(MessageConstant.STUDENT_NO_EMPTY);
        }
        if (!StringUtils.hasText(password)) {
            throw new BaseException(MessageConstant.PASSWORD_EMPTY);
        }

        // 4、校验学号和手机号唯一
        if (StringUtils.hasText(phone)) {
            User phoneUser = userMapper.getByPhone(phone);
            if (phoneUser != null) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        User studentNoUser = userMapper.getByStudentNo(studentNo);
        if (studentNoUser != null) {
            throw new BaseException(MessageConstant.STUDENT_NO_ALREADY_EXISTS);
        }

        // 5、保存用户
        String displayName = studentNo;
        if (StringUtils.hasText(name)) {
            displayName = name;
        }

        User user = new User();
        user.setStudentNo(studentNo);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setName(displayName);
        user.setPhone(phone);
        user.setRole(RoleConstant.NORMAL_USER);
        user.setStatus(UserStatusConstant.NORMAL);

        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.REGISTER_FAILED);
        }
    }

    /**
     * 查询当前用户资料。
     */
    @Override
    public UserProfileVO getCurrentUserProfile() {
        // 1、获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 2、转换并返回资料
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 修改当前用户资料。
     */
    @Override
    public void updateCurrentUserProfile(UserProfileUpdateDTO userProfileUpdateDTO) {
        // 1、校验修改参数
        if (userProfileUpdateDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 3、整理更新字段
        String name = userProfileUpdateDTO.getName();
        if (!StringUtils.hasText(name)) {
            name = null;
        }

        String phone = userProfileUpdateDTO.getPhone();
        if (StringUtils.hasText(phone)) {
            phone = phone.trim();
        } else {
            phone = null;
        }

        String avatar = userProfileUpdateDTO.getAvatar();
        if (!StringUtils.hasText(avatar)) {
            avatar = null;
        }

        String campus = userProfileUpdateDTO.getCampus();
        if (!StringUtils.hasText(campus)) {
            campus = null;
        }

        String intro = userProfileUpdateDTO.getIntro();
        if (!StringUtils.hasText(intro) && intro != null) {
            intro = null;
        }

        // 4、校验至少有一个更新字段
        if (name == null && phone == null && avatar == null && campus == null && intro == null) {
            throw new BaseException(MessageConstant.PROFILE_UPDATE_EMPTY);
        }

        // 5、校验手机号唯一
        if (StringUtils.hasText(phone)) {
            int phoneUsedCount = userMapper.countByPhoneExcludeId(phone, userId);
            if (phoneUsedCount > 0) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        // 6、执行更新
        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setPhone(phone);
        user.setAvatar(avatar);
        user.setCampus(campus);
        user.setIntro(intro);

        int rows = userMapper.updateByIdSelective(user);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.UPDATE_PROFILE_FAILED);
        }
    }

    /**
     * 注销当前用户。
     */
    @Override
    public void deleteCurrentUser() {
        // 1、获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 2、校验是否允许注销
        if (RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_SELLER);
        }

        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth != null && SellerAuthStatusConstant.PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_PENDING_AUTH);
        }

        // 3、执行注销
        int rows = userMapper.deleteById(userId);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.DELETE_USER_FAILED);
        }
    }
}
