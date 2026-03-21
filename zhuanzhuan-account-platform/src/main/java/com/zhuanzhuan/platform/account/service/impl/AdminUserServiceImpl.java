package com.zhuanzhuan.platform.account.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.PageConstant;
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

    /**
     * 新增用户。
     */
    @Override
    public void createUser(AdminUserSaveDTO saveDTO) {
        // 1、校验新增参数
        if (saveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、整理学号、手机号等关键字段，后续这些字段都要参与唯一性校验
        String studentNo = saveDTO.getStudentNo();
        if (StringUtils.hasText(studentNo)) {
            studentNo = studentNo.trim();
        } else {
            studentNo = null;
        }

        String password = saveDTO.getPassword();
        if (!StringUtils.hasText(password)) {
            password = null;
        }

        String phone = saveDTO.getPhone();
        if (StringUtils.hasText(phone)) {
            phone = phone.trim();
        } else {
            phone = null;
        }

        String name = saveDTO.getName();
        if (!StringUtils.hasText(name)) {
            name = null;
        }

        String avatar = saveDTO.getAvatar();
        if (!StringUtils.hasText(avatar)) {
            avatar = null;
        }

        String campus = saveDTO.getCampus();
        if (!StringUtils.hasText(campus)) {
            campus = null;
        }

        String intro = saveDTO.getIntro();
        if (!StringUtils.hasText(intro)) {
            intro = null;
        }

        Integer role = saveDTO.getRole();
        if (role == null) {
            role = RoleConstant.NORMAL_USER;
        }

        Integer status = saveDTO.getStatus();
        if (status == null) {
            status = UserStatusConstant.NORMAL;
        }

        // 3、校验基础信息
        if (!StringUtils.hasText(studentNo)) {
            throw new BaseException(MessageConstant.STUDENT_NO_EMPTY);
        }
        if (!StringUtils.hasText(password)) {
            throw new BaseException(MessageConstant.PASSWORD_EMPTY);
        }
        if (!isValidRole(role) || !isValidStatus(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        // 4、校验学号和手机号唯一，避免后台创建出重复账号
        User sameStudentNoUser = userMapper.getByStudentNo(studentNo);
        if (sameStudentNoUser != null) {
            throw new BaseException(MessageConstant.STUDENT_NO_ALREADY_EXISTS);
        }

        if (StringUtils.hasText(phone)) {
            User samePhoneUser = userMapper.getByPhone(phone);
            if (samePhoneUser != null) {
                throw new BaseException(MessageConstant.PHONE_ALREADY_BOUND);
            }
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
        user.setAvatar(avatar);
        user.setCampus(campus);
        user.setIntro(intro);
        user.setRole(role);
        user.setStatus(status);

        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.REGISTER_FAILED);
        }
    }

    /**
     * 修改用户。
     */
    @Override
    public void updateUser(AdminUserSaveDTO saveDTO) {
        // 1、校验修改参数
        if (saveDTO == null || saveDTO.getId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询目标用户
        User existingUser = userMapper.getById(saveDTO.getId());
        if (existingUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 3、整理更新字段，只保留本次明确传入的修改内容
        String studentNo = saveDTO.getStudentNo();
        if (StringUtils.hasText(studentNo)) {
            studentNo = studentNo.trim();
        } else {
            studentNo = null;
        }

        String password = saveDTO.getPassword();
        if (!StringUtils.hasText(password)) {
            password = null;
        }

        String phone = saveDTO.getPhone();
        if (StringUtils.hasText(phone)) {
            phone = phone.trim();
        } else {
            phone = null;
        }

        String name = saveDTO.getName();
        if (!StringUtils.hasText(name)) {
            name = null;
        }

        String avatar = saveDTO.getAvatar();
        if (!StringUtils.hasText(avatar)) {
            avatar = null;
        }

        String campus = saveDTO.getCampus();
        if (!StringUtils.hasText(campus)) {
            campus = null;
        }

        String intro = saveDTO.getIntro();
        if (!StringUtils.hasText(intro)) {
            intro = null;
        }

        Integer role = saveDTO.getRole();
        Integer status = saveDTO.getStatus();

        // 4、校验至少有一个更新字段
        if (studentNo == null
                && password == null
                && phone == null
                && name == null
                && avatar == null
                && campus == null
                && intro == null
                && role == null
                && status == null) {
            throw new BaseException(MessageConstant.PROFILE_UPDATE_EMPTY);
        }

        // 5、校验学号、手机号和状态，避免改出重复账号或非法状态
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

        if (role != null && !isValidRole(role)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }
        if (status != null && !isValidStatus(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        // 6、执行更新
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

    /**
     * 删除用户。
     */
    @Override
    public void deleteUser(Long id) {
        // 1、校验删除参数
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询目标用户
        User existingUser = userMapper.getById(id);
        if (existingUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 3、校验卖家认证流程，存在待审核申请时不允许直接删用户，避免审核数据悬空
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(id);
        if (latestAuth != null && SellerAuthStatusConstant.PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.USER_DELETE_FORBIDDEN_PENDING_AUTH);
        }

        // 4、执行删除
        int rows = userMapper.deleteById(id);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.DELETE_USER_FAILED);
        }
    }

    /**
     * 查询用户详情。
     */
    @Override
    public UserProfileVO getUserById(Long id) {
        // 1、校验查询参数
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询用户
        User user = userMapper.getById(id);
        if (user == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }

        // 3、转换返回结果
        UserProfileVO userProfileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, userProfileVO);
        return userProfileVO;
    }

    /**
     * 分页查询用户。
     */
    @Override
    public PageResult pageQueryUser(AdminUserPageQueryDTO pageQueryDTO) {
        // 1、整理分页参数
        AdminUserPageQueryDTO queryDTO = pageQueryDTO;
        if (queryDTO == null) {
            queryDTO = new AdminUserPageQueryDTO();
        }

        int page = PageConstant.DEFAULT_PAGE;
        if (queryDTO.getPage() != null) {
            page = queryDTO.getPage();
        }
        if (page < PageConstant.DEFAULT_PAGE) {
            page = PageConstant.DEFAULT_PAGE;
        }

        int pageSize = PageConstant.DEFAULT_PAGE_SIZE;
        if (queryDTO.getPageSize() != null) {
            pageSize = queryDTO.getPageSize();
        }
        if (pageSize < PageConstant.DEFAULT_PAGE) {
            pageSize = PageConstant.DEFAULT_PAGE_SIZE;
        }

        // 2、整理并校验查询条件，学号、姓名、手机号都按模糊检索的输入规则处理
        if (StringUtils.hasText(queryDTO.getStudentNo())) {
            queryDTO.setStudentNo(queryDTO.getStudentNo().trim());
        } else {
            queryDTO.setStudentNo(null);
        }

        if (StringUtils.hasText(queryDTO.getName())) {
            queryDTO.setName(queryDTO.getName().trim());
        } else {
            queryDTO.setName(null);
        }

        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryDTO.setPhone(queryDTO.getPhone().trim());
        } else {
            queryDTO.setPhone(null);
        }

        if (queryDTO.getRole() != null && !isValidRole(queryDTO.getRole())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }
        if (queryDTO.getStatus() != null && !isValidStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }

        // 3、执行分页查询
        PageHelper.startPage(page, pageSize);
        List<UserProfileVO> records = userMapper.pageQueryAdmin(queryDTO);
        Page<UserProfileVO> pageInfo = (Page<UserProfileVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 校验用户角色是否合法。
     */
    private boolean isValidRole(Integer role) {
        return RoleConstant.NORMAL_USER.equals(role)
                || RoleConstant.SELLER.equals(role);
    }

    /**
     * 校验用户状态是否合法。
     */
    private boolean isValidStatus(Integer status) {
        return UserStatusConstant.NORMAL.equals(status)
                || UserStatusConstant.BANNED.equals(status);
    }
}
