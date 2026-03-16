package com.zhuanzhuan.service.user.user.impl;

import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.UserProfileUpdateDTO;
import com.zhuanzhuan.dto.UserRegisterDTO;
import com.zhuanzhuan.entity.SellerAuth;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.mapper.sellerauth.SellerAuthMapper;
import com.zhuanzhuan.mapper.user.UserMapper;
import com.zhuanzhuan.service.user.user.UserService;
import com.zhuanzhuan.vo.UserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

/**
 * 用户资料业务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SellerAuthMapper sellerAuthMapper;

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        //1、校验注册入参对象
        if (userRegisterDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、提取并规范化注册字段
        String studentNo = trimToNull(userRegisterDTO.getStudentNo());
        String password = trimToNull(userRegisterDTO.getPassword());
        String phone = trimToNull(userRegisterDTO.getPhone());
        String name = trimToNull(userRegisterDTO.getName());

        //3、校验学号和密码必填
        if (!StringUtils.hasText(studentNo)) {
            throw new BaseException(MessageConstant.STUDENT_NO_EMPTY);
        }

        if (!StringUtils.hasText(password)) {
            throw new BaseException(MessageConstant.PASSWORD_EMPTY);
        }

        //4、校验手机号唯一性（传入时）
        if (StringUtils.hasText(phone)) {
            User phoneUser = userMapper.getByPhone(phone);
            if (phoneUser != null) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        //5、校验学号唯一性
        User studentNoUser = userMapper.getByStudentNo(studentNo);
        if (studentNoUser != null) {
            throw new BaseException(MessageConstant.STUDENT_NO_ALREADY_EXISTS);
        }

        //6、组装用户实体并执行注册
        User user = new User();
        user.setStudentNo(studentNo);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setName(StringUtils.hasText(name) ? name : studentNo);
        user.setPhone(phone);
        user.setRole(User.ROLE_NORMAL);
        user.setStatus(User.STATUS_NORMAL);

        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.REGISTER_FAILED);
        }
    }

    @Override
    public UserProfileVO getCurrentUserProfile() {
        //1、获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        //2、查询并校验当前用户
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        //3、转换并返回用户资料
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void updateCurrentUserProfile(UserProfileUpdateDTO userProfileUpdateDTO) {
        //1、校验修改入参对象
        if (userProfileUpdateDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、获取并校验当前登录用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        //3、提取并规范化待更新字段
        String name = trimToNull(userProfileUpdateDTO.getName());
        String phone = trimToNull(userProfileUpdateDTO.getPhone());
        String avatar = trimToNull(userProfileUpdateDTO.getAvatar());
        String campus = trimToNull(userProfileUpdateDTO.getCampus());
        String intro = userProfileUpdateDTO.getIntro();

        //4、校验至少有一项需要更新
        if (name == null && phone == null && avatar == null && campus == null && intro == null) {
            throw new BaseException(MessageConstant.PROFILE_UPDATE_EMPTY);
        }

        //5、校验手机号唯一性（传入时）
        if (StringUtils.hasText(phone)) {
            int phoneUsedCount = userMapper.countByPhoneExcludeId(phone, userId);
            if (phoneUsedCount > 0) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        //6、组装更新实体并执行更新
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

    @Override
    public void deleteCurrentUser() {
        //1、获取并校验当前登录用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        //2、校验账号角色是否允许注销
        if (User.ROLE_SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_SELLER);
        }

        //3、校验是否存在待审核卖家认证
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth != null && SellerAuth.STATUS_PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_PENDING_AUTH);
        }

        //4、执行账号注销
        int rows = userMapper.deleteById(userId);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.DELETE_USER_FAILED);
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
