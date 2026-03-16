package com.zhuanzhuan.service.administrator.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.PasswordConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminPageQueryDTO;
import com.zhuanzhuan.dto.AdminSaveDTO;
import com.zhuanzhuan.entity.Admin;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.mapper.administrator.AdminMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.administrator.AdminService;
import com.zhuanzhuan.vo.AdminVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理员管理业务实现
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public void createAdmin(AdminSaveDTO adminSaveDTO) {
        //1、校验新增入参对象
        if (adminSaveDTO == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、提取并规范化管理员字段
        String username = trimToNull(adminSaveDTO.getUsername());
        String password = trimToNull(adminSaveDTO.getPassword());
        String name = trimToNull(adminSaveDTO.getName());
        String phone = trimToNull(adminSaveDTO.getPhone());
        Integer status = adminSaveDTO.getStatus() == null ? Admin.STATUS_NORMAL : adminSaveDTO.getStatus();

        //3、校验账号、姓名和状态必填/合法性
        if (!StringUtils.hasText(username)) {
            throw new BaseException(MessageConstant.ADMIN_USERNAME_EMPTY);
        }

        if (!StringUtils.hasText(name)) {
            throw new BaseException(MessageConstant.ADMIN_NAME_EMPTY);
        }

        if (!isValidAdminStatus(status)) {
            throw new BaseException(MessageConstant.ADMIN_STATUS_INVALID);
        }

        //4、处理默认密码
        String rawPassword = StringUtils.hasText(password) ? password : PasswordConstant.DEFAULT_PASSWORD;

        //5、校验管理员账号唯一性
        Admin existingAdmin = adminMapper.getByUsername(username);
        if (existingAdmin != null) {
            throw new BaseException(MessageConstant.ADMIN_ALREADY_EXISTS);
        }

        //6、组装管理员实体并执行新增
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

    @Override
    public void updateAdmin(AdminSaveDTO adminSaveDTO) {
        //1、校验修改入参与目标ID
        if (adminSaveDTO == null || adminSaveDTO.getId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、查询并校验目标管理员是否存在
        Admin existingAdmin = adminMapper.getById(adminSaveDTO.getId());
        if (existingAdmin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        //3、提取并规范化待更新字段
        Long currentAdminId = BaseContext.getCurrentId();
        String username = trimToNull(adminSaveDTO.getUsername());
        String password = trimToNull(adminSaveDTO.getPassword());
        String name = trimToNull(adminSaveDTO.getName());
        String phone = trimToNull(adminSaveDTO.getPhone());
        Integer status = adminSaveDTO.getStatus();

        //4、按字段执行唯一性校验
        if (StringUtils.hasText(username)) {
            int duplicatedCount = adminMapper.countByUsernameExcludeId(username, adminSaveDTO.getId());
            if (duplicatedCount > 0) {
                throw new BaseException(MessageConstant.ADMIN_ALREADY_EXISTS);
            }
        }

        //5、校验状态更新的业务约束（禁止自禁用、至少保留一个正常管理员）
        if (status != null) {
            if (!isValidAdminStatus(status)) {
                throw new BaseException(MessageConstant.ADMIN_STATUS_INVALID);
            }
            if (Admin.STATUS_DISABLED.equals(status)) {
                if (currentAdminId != null && currentAdminId.equals(existingAdmin.getId())) {
                    throw new BaseException(MessageConstant.ADMIN_DISABLE_SELF_NOT_ALLOWED);
                }
                if (Admin.STATUS_NORMAL.equals(existingAdmin.getStatus())
                        && adminMapper.countByStatus(Admin.STATUS_NORMAL) <= 1) {
                    throw new BaseException(MessageConstant.LAST_ACTIVE_ADMIN_NOT_ALLOWED);
                }
            }
        }

        //6、组装更新实体并执行修改
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

    @Override
    public void deleteAdmin(Long id) {
        //1、校验删除ID
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、查询并校验目标管理员
        Admin existingAdmin = adminMapper.getById(id);
        if (existingAdmin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        //3、校验删除业务约束（不能删自己、至少保留一个正常管理员）
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId != null && currentAdminId.equals(id)) {
            throw new BaseException(MessageConstant.ADMIN_DELETE_SELF_NOT_ALLOWED);
        }

        if (Admin.STATUS_NORMAL.equals(existingAdmin.getStatus())
                && adminMapper.countByStatus(Admin.STATUS_NORMAL) <= 1) {
            throw new BaseException(MessageConstant.LAST_ACTIVE_ADMIN_NOT_ALLOWED);
        }

        //4、执行删除
        int rows = adminMapper.deleteById(id);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.ADMIN_DELETE_FAILED);
        }
    }

    @Override
    public AdminVO getAdminById(Long id) {
        //1、校验查询ID
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        //2、查询并校验管理员是否存在
        Admin admin = adminMapper.getById(id);
        if (admin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        //3、转换并返回查询结果
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        return adminVO;
    }

    @Override
    public PageResult pageQueryAdmin(AdminPageQueryDTO pageQueryDTO) {
        //1、处理分页入参与默认值
        AdminPageQueryDTO queryDTO = pageQueryDTO == null ? new AdminPageQueryDTO() : pageQueryDTO;
        int page = (queryDTO.getPage() == null || queryDTO.getPage() < 1) ? 1 : queryDTO.getPage();
        int pageSize = (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) ? 10 : queryDTO.getPageSize();

        //2、校验筛选状态参数
        if (queryDTO.getStatus() != null && !isValidAdminStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.ADMIN_STATUS_INVALID);
        }

        //3、执行分页查询并封装结果
        PageHelper.startPage(page, pageSize);
        List<AdminVO> records = adminMapper.pageQuery(queryDTO);
        Page<AdminVO> pageInfo = (Page<AdminVO>) records;

        return new PageResult(pageInfo.getTotal(), records);
    }

    private boolean isValidAdminStatus(Integer status) {
        return Admin.STATUS_NORMAL.equals(status) || Admin.STATUS_DISABLED.equals(status);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
