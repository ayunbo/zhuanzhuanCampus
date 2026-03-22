package com.zhuanzhuan.platform.account.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.PageConstant;
import com.zhuanzhuan.constant.RoleConstant;
import com.zhuanzhuan.constant.SellerAuthStatusConstant;
import com.zhuanzhuan.constant.UserStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminSellerAuthPageQueryDTO;
import com.zhuanzhuan.dto.SellerAuthApplyDTO;
import com.zhuanzhuan.dto.SellerAuthAuditDTO;
import com.zhuanzhuan.entity.SellerAuth;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.platform.account.mapper.SellerAuthMapper;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.account.service.SellerAuthService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.utils.IdGenerator;
import com.zhuanzhuan.vo.SellerAuthResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 卖家认证业务实现。
 */
@Service
public class SellerAuthServiceImpl implements SellerAuthService {

    @Autowired
    private SellerAuthMapper sellerAuthMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 提交卖家认证。
     */
    @Override
    public void submitSellerAuth(SellerAuthApplyDTO sellerAuthApplyDTO) {
        // 1、校验提交参数
        if (sellerAuthApplyDTO == null) {
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
        if (!UserStatusConstant.NORMAL.equals(currentUser.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3、校验是否允许发起卖家认证
        if (!RoleConstant.NORMAL_USER.equals(currentUser.getRole())) {
            if (RoleConstant.SELLER.equals(currentUser.getRole())) {
                throw new BaseException(MessageConstant.ALREADY_SELLER);
            }
            throw new BaseException(MessageConstant.ROLE_NOT_ALLOW_APPLY);
        }

        // 4、整理申请字段，实名、手机号和认证材料后面都会作为审核依据
        String realName = sellerAuthApplyDTO.getRealName();
        if (!StringUtils.hasText(realName)) {
            realName = null;
        }

        String phone = sellerAuthApplyDTO.getPhone();
        if (StringUtils.hasText(phone)) {
            phone = phone.trim();
        } else {
            phone = null;
        }

        String material = sellerAuthApplyDTO.getMaterial();
        if (!StringUtils.hasText(material)) {
            material = null;
        }

        // 5、校验申请信息
        if (!StringUtils.hasText(realName)) {
            throw new BaseException(MessageConstant.REAL_NAME_EMPTY);
        }
        if (!StringUtils.hasText(phone)) {
            throw new BaseException(MessageConstant.PHONE_EMPTY);
        }
        if (!StringUtils.hasText(material)) {
            throw new BaseException(MessageConstant.MATERIAL_EMPTY);
        }

        // 6、校验是否已有进行中的认证流程，同一用户不能同时提交多条待审核申请
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth != null && SellerAuthStatusConstant.PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_PENDING);
        }
        if (latestAuth != null && SellerAuthStatusConstant.APPROVED.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_APPROVED);
        }

        // 7、保存认证申请
        SellerAuth sellerAuth = new SellerAuth();
        sellerAuth.setId(IdGenerator.nextId());
        sellerAuth.setUserId(userId);
        sellerAuth.setRealName(realName);
        sellerAuth.setStudentNo(currentUser.getStudentNo());
        sellerAuth.setPhone(phone);
        sellerAuth.setMaterial(material);
        sellerAuth.setStatus(SellerAuthStatusConstant.PENDING);

        int rows = sellerAuthMapper.insert(sellerAuth);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.SELLER_AUTH_SUBMIT_FAILED);
        }
    }

    /**
     * 查询当前认证结果。
     */
    @Override
    public SellerAuthResultVO getCurrentSellerAuthResult() {
        // 1、获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、查询最新认证记录
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth == null) {
            throw new BaseException(MessageConstant.NO_SELLER_AUTH_RECORD);
        }

        // 3、转换返回结果
        SellerAuthResultVO vo = new SellerAuthResultVO();
        BeanUtils.copyProperties(latestAuth, vo);
        vo.setStatusDesc(toStatusDesc(latestAuth.getStatus()));
        return vo;
    }

    /**
     * 校验当前用户是否为卖家。
     */
    @Override
    public void checkCurrentUserIsSeller() {
        // 1、获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、校验当前账号角色
        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (!RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
    }

    /**
     * 分页查询卖家认证。
     */
    @Override
    public PageResult pageQuerySellerAuth(AdminSellerAuthPageQueryDTO pageQueryDTO) {
        // 1、整理分页参数
        AdminSellerAuthPageQueryDTO queryDTO = pageQueryDTO;
        if (queryDTO == null) {
            queryDTO = new AdminSellerAuthPageQueryDTO();
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

        // 2、整理并校验查询条件，方便后台按姓名、手机号、学号筛选申请记录
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

        if (StringUtils.hasText(queryDTO.getStudentNo())) {
            queryDTO.setStudentNo(queryDTO.getStudentNo().trim());
        } else {
            queryDTO.setStudentNo(null);
        }

        if (queryDTO.getStatus() != null && !isValidSellerAuthStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.SELLER_AUTH_STATUS_INVALID);
        }

        // 3、执行分页查询
        PageHelper.startPage(page, pageSize);
        List<SellerAuthResultVO> records = sellerAuthMapper.pageQuery(queryDTO);
        Page<SellerAuthResultVO> pageInfo = (Page<SellerAuthResultVO>) records;

        // 4、补充状态文案
        for (SellerAuthResultVO record : records) {
            record.setStatusDesc(toStatusDesc(record.getStatus()));
        }

        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 审核卖家认证。
     */
    @Override
    @Transactional
    public void auditSellerAuth(SellerAuthAuditDTO sellerAuthAuditDTO) {
        // 1、校验审核参数
        if (sellerAuthAuditDTO == null
                || sellerAuthAuditDTO.getAuthId() == null
                || sellerAuthAuditDTO.getStatus() == null) {
            throw new BaseException(MessageConstant.AUDIT_PARAM_INCOMPLETE);
        }

        Integer targetStatus = sellerAuthAuditDTO.getStatus();
        if (!SellerAuthStatusConstant.APPROVED.equals(targetStatus)
                && !SellerAuthStatusConstant.REJECTED.equals(targetStatus)) {
            throw new BaseException(MessageConstant.AUDIT_STATUS_INVALID);
        }

        // 2、整理驳回原因，只有审核驳回时这个字段才允许落库并回显给用户
        String rejectReason = sellerAuthAuditDTO.getReason();
        if (!StringUtils.hasText(rejectReason)) {
            rejectReason = null;
        }

        if (SellerAuthStatusConstant.REJECTED.equals(targetStatus) && !StringUtils.hasText(rejectReason)) {
            throw new BaseException(MessageConstant.REJECT_REASON_REQUIRED);
        }

        // 3、查询待审核记录
        SellerAuth sellerAuth = sellerAuthMapper.getById(sellerAuthAuditDTO.getAuthId());
        if (sellerAuth == null) {
            throw new BaseException(MessageConstant.AUTH_NOT_FOUND);
        }
        if (!SellerAuthStatusConstant.PENDING.equals(sellerAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_AUDITED);
        }

        // 4、获取当前登录管理员
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        // 5、审核通过前校验用户状态，只有正常账号才能升级成卖家角色
        if (SellerAuthStatusConstant.APPROVED.equals(targetStatus)) {
            User authUser = userMapper.getById(sellerAuth.getUserId());
            if (authUser == null) {
                throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
            }
            if (!UserStatusConstant.NORMAL.equals(authUser.getStatus())) {
                throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
            }
        }

        // 6、更新认证记录，写入审核人、审核时间和最终审核结果
        SellerAuth updateEntity = new SellerAuth();
        updateEntity.setId(sellerAuthAuditDTO.getAuthId());
        updateEntity.setStatus(targetStatus);
        if (SellerAuthStatusConstant.REJECTED.equals(targetStatus)) {
            updateEntity.setReason(rejectReason);
        } else {
            updateEntity.setReason(null);
        }
        updateEntity.setAuditAdminId(adminId);
        updateEntity.setAuditTime(LocalDateTime.now());

        int rows = sellerAuthMapper.updateAuditById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.AUDIT_FAILED);
        }

        // 7、审核通过后同步更新用户角色，卖家权限从这里开始生效
        if (SellerAuthStatusConstant.APPROVED.equals(targetStatus)) {
            int roleRows = userMapper.updateRoleById(sellerAuth.getUserId(), RoleConstant.SELLER);
            if (roleRows <= 0) {
                throw new BaseException(MessageConstant.AUDIT_FAILED);
            }
        }
    }

    /**
     * 校验卖家认证状态是否合法。
     */
    private boolean isValidSellerAuthStatus(Integer status) {
        return SellerAuthStatusConstant.PENDING.equals(status)
                || SellerAuthStatusConstant.APPROVED.equals(status)
                || SellerAuthStatusConstant.REJECTED.equals(status)
                || SellerAuthStatusConstant.REVOKED.equals(status);
    }

    /**
     * 转换卖家认证状态文案。
     */
    private String toStatusDesc(Integer status) {
        if (SellerAuthStatusConstant.PENDING.equals(status)) {
            return SellerAuthStatusConstant.PENDING_DESC;
        }
        if (SellerAuthStatusConstant.APPROVED.equals(status)) {
            return SellerAuthStatusConstant.APPROVED_DESC;
        }
        if (SellerAuthStatusConstant.REJECTED.equals(status)) {
            return SellerAuthStatusConstant.REJECTED_DESC;
        }
        if (SellerAuthStatusConstant.REVOKED.equals(status)) {
            return SellerAuthStatusConstant.REVOKED_DESC;
        }
        return SellerAuthStatusConstant.UNKNOWN_DESC;
    }
}
