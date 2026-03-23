package com.zhuanzhuan.platform.account.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.AdminConstant;
import com.zhuanzhuan.constant.AdminStatusConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.PageConstant;
import com.zhuanzhuan.constant.PasswordConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminPageQueryDTO;
import com.zhuanzhuan.dto.AdminSaveDTO;
import com.zhuanzhuan.entity.Admin;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.account.mapper.AdminMapper;
import com.zhuanzhuan.platform.account.service.AdminService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.AdminVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理员业务实现。
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 新增管理员。
     */
    @Override
    public void createAdmin(AdminSaveDTO adminSaveDTO) {
        // 1、校验新增参数
        if (adminSaveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、整理请求字段
        String username = adminSaveDTO.getUsername();
        if (StringUtils.hasText(username)) {
            username = username.trim();
        } else {
            username = null;
        }

        String password = adminSaveDTO.getPassword();
        if (!StringUtils.hasText(password)) {
            password = null;
        }

        String name = adminSaveDTO.getName();
        if (!StringUtils.hasText(name)) {
            name = null;
        }

        String phone = adminSaveDTO.getPhone();
        if (StringUtils.hasText(phone)) {
            phone = phone.trim();
        } else {
            phone = null;
        }

        Integer status = adminSaveDTO.getStatus();
        if (status == null) {
            status = AdminStatusConstant.NORMAL;
        }

        // 3、校验基础信息
        if (!StringUtils.hasText(username)) {
            throw new BaseException(MessageConstant.ADMIN_USERNAME_EMPTY);
        }
        if (!StringUtils.hasText(name)) {
            throw new BaseException(MessageConstant.ADMIN_NAME_EMPTY);
        }
        if (!isValidAdminStatus(status)) {
            throw new BaseException(MessageConstant.ADMIN_STATUS_INVALID);
        }

        // 4、处理默认密码并校验账号唯一
        String rawPassword = password;
        if (!StringUtils.hasText(rawPassword)) {
            rawPassword = PasswordConstant.DEFAULT_PASSWORD;
        }

        Admin existingAdmin = adminMapper.getByUsername(username);
        if (existingAdmin != null) {
            throw new BaseException(MessageConstant.ADMIN_ALREADY_EXISTS);
        }

        // 5、保存管理员
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(DigestUtils.md5DigestAsHex(rawPassword.getBytes()));
        admin.setName(name);
        admin.setPhone(phone);
        admin.setStatus(status);

        int rows = adminMapper.insert(admin);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.ADMIN_CREATE_FAILED);
        }
    }

    /**
     * 修改管理员。
     */
    @Override
    public void updateAdmin(AdminSaveDTO adminSaveDTO) {
        // 1、校验修改参数
        if (adminSaveDTO == null || adminSaveDTO.getId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询目标管理员
        Admin existingAdmin = adminMapper.getById(adminSaveDTO.getId());
        if (existingAdmin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        // 3、整理更新字段
        Long currentAdminId = BaseContext.getCurrentId();

        String username = adminSaveDTO.getUsername();
        if (StringUtils.hasText(username)) {
            username = username.trim();
        } else {
            username = null;
        }

        String password = adminSaveDTO.getPassword();
        if (!StringUtils.hasText(password)) {
            password = null;
        }

        String name = adminSaveDTO.getName();
        if (!StringUtils.hasText(name)) {
            name = null;
        }

        String phone = adminSaveDTO.getPhone();
        if (StringUtils.hasText(phone)) {
            phone = phone.trim();
        } else {
            phone = null;
        }

        Integer status = adminSaveDTO.getStatus();

        // 4、校验唯一性和状态约束
        if (StringUtils.hasText(username)) {
            int duplicatedCount = adminMapper.countByUsernameExcludeId(username, adminSaveDTO.getId());
            if (duplicatedCount > 0) {
                throw new BaseException(MessageConstant.ADMIN_ALREADY_EXISTS);
            }
        }

        if (status != null) {
            if (!isValidAdminStatus(status)) {
                throw new BaseException(MessageConstant.ADMIN_STATUS_INVALID);
            }

            if (AdminStatusConstant.DISABLED.equals(status)) {
                if (currentAdminId != null && currentAdminId.equals(existingAdmin.getId())) {
                    throw new BaseException(MessageConstant.ADMIN_DISABLE_SELF_NOT_ALLOWED);
                }

                if (AdminStatusConstant.NORMAL.equals(existingAdmin.getStatus())
                        && adminMapper.countByStatus(AdminStatusConstant.NORMAL) <= AdminConstant.MIN_ACTIVE_ADMIN_COUNT) {
                    throw new BaseException(MessageConstant.LAST_ACTIVE_ADMIN_NOT_ALLOWED);
                }
            }
        }

        // 5、执行更新
        Admin admin = new Admin();
        admin.setId(adminSaveDTO.getId());
        admin.setUsername(username);
        admin.setName(name);
        admin.setPhone(phone);
        admin.setStatus(status);

        if (StringUtils.hasText(password)) {
            admin.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        }

        int rows = adminMapper.updateByIdSelective(admin);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.ADMIN_UPDATE_FAILED);
        }
    }

    /**
     * 删除管理员。
     */
    @Override
    public void deleteAdmin(Long id) {
        // 1、校验删除参数
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询目标管理员
        Admin existingAdmin = adminMapper.getById(id);
        if (existingAdmin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        // 3、校验删除约束
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId != null && currentAdminId.equals(id)) {
            throw new BaseException(MessageConstant.ADMIN_DELETE_SELF_NOT_ALLOWED);
        }

        if (AdminStatusConstant.NORMAL.equals(existingAdmin.getStatus())
                && adminMapper.countByStatus(AdminStatusConstant.NORMAL) <= AdminConstant.MIN_ACTIVE_ADMIN_COUNT) {
            throw new BaseException(MessageConstant.LAST_ACTIVE_ADMIN_NOT_ALLOWED);
        }

        // 4、执行删除
        int rows = adminMapper.deleteById(id);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.ADMIN_DELETE_FAILED);
        }
    }

    /**
     * 查询管理员详情。
     */
    @Override
    public AdminVO getAdminById(Long id) {
        // 1、校验查询参数
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        // 2、查询管理员
        Admin admin = adminMapper.getById(id);
        if (admin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        // 3、转换返回结果
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        return adminVO;
    }

    /**
     * 分页查询管理员。
     */
    @Override
    public PageResult pageQueryAdmin(AdminPageQueryDTO pageQueryDTO) {
        // 1、整理分页参数
        AdminPageQueryDTO queryDTO = pageQueryDTO;
        if (queryDTO == null) {
            queryDTO = new AdminPageQueryDTO();
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

        // 2、校验查询条件
        if (queryDTO.getStatus() != null && !isValidAdminStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.ADMIN_STATUS_INVALID);
        }

        // 3、执行分页查询
        PageHelper.startPage(page, pageSize);
        List<AdminVO> records = adminMapper.pageQuery(queryDTO);
        Page<AdminVO> pageInfo = (Page<AdminVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 校验管理员状态是否合法。
     */
    private boolean isValidAdminStatus(Integer status) {
        return AdminStatusConstant.NORMAL.equals(status)
                || AdminStatusConstant.DISABLED.equals(status);
    }
}
