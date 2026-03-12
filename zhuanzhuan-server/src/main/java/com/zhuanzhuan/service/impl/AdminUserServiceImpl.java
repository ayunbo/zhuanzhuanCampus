package com.zhuanzhuan.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.dto.AdminUserPageQueryDTO;
import com.zhuanzhuan.dto.AdminUserSaveDTO;
import com.zhuanzhuan.entity.SellerAuth;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.mapper.SellerAuthMapper;
import com.zhuanzhuan.mapper.UserMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.AdminUserService;
import com.zhuanzhuan.vo.UserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SellerAuthMapper sellerAuthMapper;

    @Override
    public void createUser(AdminUserSaveDTO saveDTO) {
        //1、校验新增入参
        if (saveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、提取并校验必填字段
        String studentNo = trimToNull(saveDTO.getStudentNo());
        String password = trimToNull(saveDTO.getPassword());
        if (!StringUtils.hasText(studentNo)) {
            throw new BaseException(MessageConstant.STUDENT_NO_EMPTY);
        }
        if (!StringUtils.hasText(password)) {
            throw new BaseException(MessageConstant.PASSWORD_EMPTY);
        }

        //3、校验学号和手机号唯一性
        User sameStudentNoUser = userMapper.getByStudentNo(studentNo);
        if (sameStudentNoUser != null) {
            throw new BaseException(MessageConstant.STUDENT_NO_ALREADY_EXISTS);
        }

        String phone = trimToNull(saveDTO.getPhone());
        if (StringUtils.hasText(phone)) {
            User samePhoneUser = userMapper.getByPhone(phone);
            if (samePhoneUser != null) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        //4、处理角色与状态默认值并校验范围
        Integer role = saveDTO.getRole() == null ? User.ROLE_NORMAL : saveDTO.getRole();
        Integer status = saveDTO.getStatus() == null ? User.STATUS_NORMAL : saveDTO.getStatus();
        if (!isValidRole(role) || !isValidStatus(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        //5、组装实体并新增用户
        User user = new User();
        user.setStudentNo(studentNo);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setName(StringUtils.hasText(saveDTO.getName()) ? saveDTO.getName().trim() : studentNo);
        user.setPhone(phone);
        user.setAvatar(trimToNull(saveDTO.getAvatar()));
        user.setCampus(trimToNull(saveDTO.getCampus()));
        user.setIntro(trimToNull(saveDTO.getIntro()));
        user.setRole(role);
        user.setStatus(status);

        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.REGISTER_FAILED);
        }
    }

    @Override
    public void updateUser(AdminUserSaveDTO saveDTO) {
        //1、校验更新入参
        if (saveDTO == null || saveDTO.getId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、查询并校验目标用户
        User existingUser = userMapper.getById(saveDTO.getId());
        if (existingUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        //3、提取并校验基础字段
        String studentNo = trimToNull(saveDTO.getStudentNo());
        String password = trimToNull(saveDTO.getPassword());
        String phone = trimToNull(saveDTO.getPhone());
        String name = trimToNull(saveDTO.getName());
        String avatar = trimToNull(saveDTO.getAvatar());
        String campus = trimToNull(saveDTO.getCampus());
        String intro = trimToNull(saveDTO.getIntro());
        Integer role = saveDTO.getRole();
        Integer status = saveDTO.getStatus();

        if (studentNo == null && password == null && phone == null && name == null
                && avatar == null && campus == null && intro == null
                && role == null && status == null) {
            throw new BaseException(MessageConstant.PROFILE_UPDATE_EMPTY);
        }

        //4、校验学号和手机号唯一性
        if (StringUtils.hasText(studentNo) && !studentNo.equals(existingUser.getStudentNo())) {
            User sameStudentNoUser = userMapper.getByStudentNo(studentNo);
            if (sameStudentNoUser != null) {
                throw new BaseException(MessageConstant.STUDENT_NO_ALREADY_EXISTS);
            }
        }

        if (StringUtils.hasText(phone)) {
            int phoneCount = userMapper.countByPhoneExcludeId(phone, saveDTO.getId());
            if (phoneCount > 0) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
        }

        //5、校验角色和状态范围
        if (role != null && !isValidRole(role)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }
        if (status != null && !isValidStatus(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        //6、组装实体并更新用户
        User updateEntity = new User();
        updateEntity.setId(saveDTO.getId());
        updateEntity.setStudentNo(studentNo);
        updateEntity.setName(name);
        updateEntity.setPhone(phone);
        updateEntity.setAvatar(avatar);
        updateEntity.setCampus(campus);
        updateEntity.setIntro(intro);
        updateEntity.setRole(role);
        updateEntity.setStatus(status);
        if (StringUtils.hasText(password)) {
            updateEntity.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        }

        int rows = userMapper.updateByIdSelective(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.UPDATE_PROFILE_FAILED);
        }
    }

    @Override
    public void deleteUser(Long id) {
        //1、校验删除ID
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、校验用户存在
        User existingUser = userMapper.getById(id);
        if (existingUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        //3、校验关键业务约束（待审核认证不能删除）
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(id);
        if (latestAuth != null && SellerAuth.STATUS_PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_PENDING_AUTH);
        }

        //4、执行删除
        int rows = userMapper.deleteById(id);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.DELETE_USER_FAILED);
        }
    }

    @Override
    public UserProfileVO getUserById(Long id) {
        //1、校验查询ID
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、查询并校验用户
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        //3、转换并返回用户详情
        UserProfileVO userProfileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, userProfileVO);
        return userProfileVO;
    }

    @Override
    public PageResult pageQueryUser(AdminUserPageQueryDTO pageQueryDTO) {
        //1、处理分页参数
        AdminUserPageQueryDTO queryDTO = pageQueryDTO == null ? new AdminUserPageQueryDTO() : pageQueryDTO;
        int page = (queryDTO.getPage() == null || queryDTO.getPage() < 1) ? 1 : queryDTO.getPage();
        int pageSize = (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) ? 10 : queryDTO.getPageSize();

        //2、校验筛选字段
        if (queryDTO.getRole() != null && !isValidRole(queryDTO.getRole())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }
        if (queryDTO.getStatus() != null && !isValidStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        queryDTO.setStudentNo(trimToNull(queryDTO.getStudentNo()));
        queryDTO.setName(trimToNull(queryDTO.getName()));
        queryDTO.setPhone(trimToNull(queryDTO.getPhone()));

        //3、执行分页查询并返回
        PageHelper.startPage(page, pageSize);
        List<UserProfileVO> records = userMapper.pageQueryAdmin(queryDTO);
        Page<UserProfileVO> pageInfo = (Page<UserProfileVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    private boolean isValidRole(Integer role) {
        return User.ROLE_NORMAL.equals(role) || User.ROLE_SELLER.equals(role);
    }

    private boolean isValidStatus(Integer status) {
        return User.STATUS_NORMAL.equals(status) || User.STATUS_BANNED.equals(status);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
