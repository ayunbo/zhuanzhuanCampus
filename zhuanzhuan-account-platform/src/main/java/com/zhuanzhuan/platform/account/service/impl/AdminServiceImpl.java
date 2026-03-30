package com.zhuanzhuan.platform.account.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.AdminConstant;
import com.zhuanzhuan.constant.AdminStatusConstant;
import com.zhuanzhuan.constant.MessageConstant;
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
     *
     * @param adminSaveDTO 管理员新增信息
     */
    @Override
    public void createAdmin(AdminSaveDTO adminSaveDTO) {
        // 1、校验账号唯一性，同一用户名不允许重复注册
        Admin existingAdmin = adminMapper.getByUsername(adminSaveDTO.getUsername());
        if (existingAdmin != null) {
            throw new BaseException(MessageConstant.ADMIN_ALREADY_EXISTS);
        }

        // 2、将 DTO 属性拷贝到实体对象
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminSaveDTO, admin);

        // 3、处理密码：未传密码时使用系统默认密码，密码统一 MD5 加密后存储
        String rawPassword = StringUtils.hasText(adminSaveDTO.getPassword())
                ? adminSaveDTO.getPassword()
                : PasswordConstant.DEFAULT_PASSWORD;
        admin.setPassword(DigestUtils.md5DigestAsHex(rawPassword.getBytes()));

        // 4、如果未指定状态，默认为正常状态
        if (admin.getStatus() == null) {
            admin.setStatus(AdminStatusConstant.NORMAL);
        }

        // 5、执行数据库插入
        adminMapper.insert(admin);
    }

    /**
     * 修改管理员。
     *
     * @param adminSaveDTO 管理员修改信息（需携带 id）
     */
    @Override
    public void updateAdmin(AdminSaveDTO adminSaveDTO) {
        // 1、查询目标管理员，确认记录存在
        Admin existingAdmin = adminMapper.getById(adminSaveDTO.getId());
        if (existingAdmin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        // 2、如果修改了用户名，校验新用户名是否与其他账号冲突
        if (StringUtils.hasText(adminSaveDTO.getUsername())) {
            int duplicatedCount = adminMapper.countByUsernameExcludeId(adminSaveDTO.getUsername(), adminSaveDTO.getId());
            if (duplicatedCount > 0) {
                throw new BaseException(MessageConstant.ADMIN_ALREADY_EXISTS);
            }
        }

        // 3、校验禁用约束：不能禁用当前登录管理员，也不能禁用最后一个正常账号
        Integer status = adminSaveDTO.getStatus();
        if (AdminStatusConstant.DISABLED.equals(status)) {
            Long currentAdminId = BaseContext.getCurrentId();
            if (currentAdminId != null && currentAdminId.equals(existingAdmin.getId())) {
                throw new BaseException(MessageConstant.ADMIN_DISABLE_SELF_NOT_ALLOWED);
            }
            if (AdminStatusConstant.NORMAL.equals(existingAdmin.getStatus())
                    && adminMapper.countByStatus(AdminStatusConstant.NORMAL) <= AdminConstant.MIN_ACTIVE_ADMIN_COUNT) {
                throw new BaseException(MessageConstant.LAST_ACTIVE_ADMIN_NOT_ALLOWED);
            }
        }

        // 4、将 DTO 属性拷贝到更新实体
        Admin admin = new Admin();
        BeanUtils.copyProperties(adminSaveDTO, admin);

        // 5、如果本次携带了新密码，则更新为新密码的 MD5 值；否则不修改密码字段
        if (StringUtils.hasText(adminSaveDTO.getPassword())) {
            admin.setPassword(DigestUtils.md5DigestAsHex(adminSaveDTO.getPassword().getBytes()));
        } else {
            admin.setPassword(null);
        }

        // 6、执行选择性更新（只更新非 null 字段）
        adminMapper.updateByIdSelective(admin);
    }

    /**
     * 删除管理员。
     *
     * @param id 管理员 ID
     */
    @Override
    public void deleteAdmin(Long id) {
        // 1、查询目标管理员，确认记录存在
        Admin existingAdmin = adminMapper.getById(id);
        if (existingAdmin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        // 2、不允许删除当前登录管理员自身
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId != null && currentAdminId.equals(id)) {
            throw new BaseException(MessageConstant.ADMIN_DELETE_SELF_NOT_ALLOWED);
        }

        // 3、不允许删除最后一个正常状态的管理员，保证系统始终有管理员可登录
        if (AdminStatusConstant.NORMAL.equals(existingAdmin.getStatus())
                && adminMapper.countByStatus(AdminStatusConstant.NORMAL) <= AdminConstant.MIN_ACTIVE_ADMIN_COUNT) {
            throw new BaseException(MessageConstant.LAST_ACTIVE_ADMIN_NOT_ALLOWED);
        }

        // 4、执行物理删除
        adminMapper.deleteById(id);
    }

    /**
     * 根据 ID 查询管理员详情。
     *
     * @param id 管理员 ID
     * @return 管理员视图对象
     */
    @Override
    public AdminVO getAdminById(Long id) {
        // 1、查询管理员，不存在则抛出业务异常
        Admin admin = adminMapper.getById(id);
        if (admin == null) {
            throw new BaseException(MessageConstant.ADMIN_NOT_FOUND);
        }

        // 2、将实体属性拷贝到 VO 并返回
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        return adminVO;
    }

    /**
     * 分页查询管理员列表。
     *
     * @param pageQueryDTO 分页查询条件（page、pageSize 已有默认值）
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryAdmin(AdminPageQueryDTO pageQueryDTO) {
        // 1、启动分页插件，page 和 pageSize 由 DTO 默认值保证非空
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 2、执行查询，PageHelper 自动拦截并追加 LIMIT
        List<AdminVO> records = adminMapper.pageQuery(pageQueryDTO);
        Page<AdminVO> pageInfo = (Page<AdminVO>) records;

        // 3、封装总记录数和当前页数据返回
        return new PageResult(pageInfo.getTotal(), records);
    }
}
