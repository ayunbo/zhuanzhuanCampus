package com.zhuanzhuan.service.sellerauth.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminSellerAuthPageQueryDTO;
import com.zhuanzhuan.dto.SellerAuthApplyDTO;
import com.zhuanzhuan.dto.SellerAuthAuditDTO;
import com.zhuanzhuan.entity.SellerAuth;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.mapper.sellerauth.SellerAuthMapper;
import com.zhuanzhuan.mapper.user.UserMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.sellerauth.SellerAuthService;
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
 * 卖家认证业务实现
 */
@Service
public class SellerAuthServiceImpl implements SellerAuthService {

    @Autowired
    private SellerAuthMapper sellerAuthMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void submitSellerAuth(SellerAuthApplyDTO sellerAuthApplyDTO) {
        //1、校验申请入参对象
        if (sellerAuthApplyDTO == null) {
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
        if (!User.STATUS_NORMAL.equals(currentUser.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、校验当前账号角色是否允许申请
        if (!User.ROLE_NORMAL.equals(currentUser.getRole())) {
            if (User.ROLE_SELLER.equals(currentUser.getRole())) {
                throw new BaseException(MessageConstant.ALREADY_SELLER);
            }
            throw new BaseException(MessageConstant.ROLE_NOT_ALLOW_APPLY);
        }

        //4、提取并规范化申请字段
        String realName = trimToNull(sellerAuthApplyDTO.getRealName());
        String phone = trimToNull(sellerAuthApplyDTO.getPhone());
        String material = trimToNull(sellerAuthApplyDTO.getMaterial());

        //5、校验姓名、手机号、材料必填
        if (!StringUtils.hasText(realName)) {
            throw new BaseException(MessageConstant.REAL_NAME_EMPTY);
        }

        if (!StringUtils.hasText(phone)) {
            throw new BaseException(MessageConstant.PHONE_EMPTY);
        }

        if (!StringUtils.hasText(material)) {
            throw new BaseException(MessageConstant.MATERIAL_EMPTY);
        }

        //6、校验历史申请状态（不可重复待审/重复通过）
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth != null && SellerAuth.STATUS_PENDING.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_PENDING);
        }
        if (latestAuth != null && SellerAuth.STATUS_APPROVED.equals(latestAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_APPROVED);
        }

        //7、组装申请实体并提交
        SellerAuth sellerAuth = new SellerAuth();
        sellerAuth.setId(IdGenerator.nextId());
        sellerAuth.setUserId(userId);
        sellerAuth.setRealName(realName);
        sellerAuth.setStudentNo(currentUser.getStudentNo());
        sellerAuth.setPhone(phone);
        sellerAuth.setMaterial(material);
        sellerAuth.setStatus(SellerAuth.STATUS_PENDING);

        int rows = sellerAuthMapper.insert(sellerAuth);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.SELLER_AUTH_SUBMIT_FAILED);
        }
    }

    @Override
    public SellerAuthResultVO getCurrentSellerAuthResult() {
        //1、获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        //2、查询最近一次认证记录并校验
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth == null) {
            throw new BaseException(MessageConstant.NO_SELLER_AUTH_RECORD);
        }

        //3、转换并返回认证结果
        SellerAuthResultVO vo = new SellerAuthResultVO();
        BeanUtils.copyProperties(latestAuth, vo);
        vo.setStatusDesc(toStatusDesc(latestAuth.getStatus()));
        return vo;
    }

    @Override
    public void checkCurrentUserIsSeller() {
        //1、获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        //2、校验用户存在且角色为卖家
        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (!User.ROLE_SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
    }

    @Override
    public PageResult pageQuerySellerAuth(AdminSellerAuthPageQueryDTO pageQueryDTO) {
        //1、处理分页入参与默认值
        AdminSellerAuthPageQueryDTO queryDTO = pageQueryDTO == null ? new AdminSellerAuthPageQueryDTO() : pageQueryDTO;
        int page = (queryDTO.getPage() == null || queryDTO.getPage() < 1) ? 1 : queryDTO.getPage();
        int pageSize = (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) ? 10 : queryDTO.getPageSize();

        //2、校验并处理认证状态筛选条件
        if (queryDTO.getStatus() == null) {
            queryDTO.setStatus(SellerAuth.STATUS_PENDING);
        } else if (!isValidSellerAuthStatus(queryDTO.getStatus())) {
            throw new BaseException(MessageConstant.SELLER_AUTH_STATUS_INVALID);
        }

        //3、规范化可选筛选字段
        queryDTO.setName(trimToNull(queryDTO.getName()));
        queryDTO.setPhone(trimToNull(queryDTO.getPhone()));
        queryDTO.setStudentNo(trimToNull(queryDTO.getStudentNo()));

        //4、执行分页查询并补充状态描述
        PageHelper.startPage(page, pageSize);
        List<SellerAuthResultVO> records = sellerAuthMapper.pageQuery(queryDTO);
        Page<SellerAuthResultVO> pageInfo = (Page<SellerAuthResultVO>) records;

        for (SellerAuthResultVO record : records) {
            record.setStatusDesc(toStatusDesc(record.getStatus()));
        }

        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    @Transactional
    public void auditSellerAuth(SellerAuthAuditDTO sellerAuthAuditDTO) {
        //1、校验审核入参与必要字段
        if (sellerAuthAuditDTO == null
                || sellerAuthAuditDTO.getAuthId() == null
                || sellerAuthAuditDTO.getStatus() == null) {
            throw new BaseException(MessageConstant.AUDIT_PARAM_INCOMPLETE);
        }

        //2、校验审核目标状态合法性
        Integer targetStatus = sellerAuthAuditDTO.getStatus();
        if (!SellerAuth.STATUS_APPROVED.equals(targetStatus)
                && !SellerAuth.STATUS_REJECTED.equals(targetStatus)) {
            throw new BaseException(MessageConstant.AUDIT_STATUS_INVALID);
        }

        //3、驳回场景校验驳回原因
        String rejectReason = trimToNull(sellerAuthAuditDTO.getReason());
        if (SellerAuth.STATUS_REJECTED.equals(targetStatus)) {
            if (!StringUtils.hasText(rejectReason)) {
                throw new BaseException(MessageConstant.REJECT_REASON_REQUIRED);
            }
        }

        //4、查询并校验认证申请状态
        SellerAuth sellerAuth = sellerAuthMapper.getById(sellerAuthAuditDTO.getAuthId());
        if (sellerAuth == null) {
            throw new BaseException(MessageConstant.AUTH_NOT_FOUND);
        }
        if (!SellerAuth.STATUS_PENDING.equals(sellerAuth.getStatus())) {
            throw new BaseException(MessageConstant.AUTH_ALREADY_AUDITED);
        }

        //5、校验管理员登录态
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        //6、通过场景校验被审核用户状态
        if (SellerAuth.STATUS_APPROVED.equals(targetStatus)) {
            User authUser = userMapper.getById(sellerAuth.getUserId());
            if (authUser == null) {
                throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
            }
            if (!User.STATUS_NORMAL.equals(authUser.getStatus())) {
                throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
            }
        }

        //7、更新认证审核结果
        SellerAuth updateEntity = new SellerAuth();
        updateEntity.setId(sellerAuthAuditDTO.getAuthId());
        updateEntity.setStatus(targetStatus);
        updateEntity.setReason(SellerAuth.STATUS_REJECTED.equals(targetStatus) ? rejectReason : null);
        updateEntity.setAuditAdminId(adminId);
        updateEntity.setAuditTime(LocalDateTime.now());

        int rows = sellerAuthMapper.updateAuditById(updateEntity);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.AUDIT_FAILED);
        }

        //8、审核通过后升级用户为卖家
        if (SellerAuth.STATUS_APPROVED.equals(targetStatus)) {
            int roleRows = userMapper.updateRoleById(sellerAuth.getUserId(), User.ROLE_SELLER);
            if (roleRows <= 0) {
                throw new BaseException(MessageConstant.AUDIT_FAILED);
            }
        }
    }

    private boolean isValidSellerAuthStatus(Integer status) {
        return SellerAuth.STATUS_PENDING.equals(status)
                || SellerAuth.STATUS_APPROVED.equals(status)
                || SellerAuth.STATUS_REJECTED.equals(status)
                || SellerAuth.STATUS_REVOKED.equals(status);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String toStatusDesc(Integer status) {
        if (SellerAuth.STATUS_PENDING.equals(status)) {
            return "待审核";
        }
        if (SellerAuth.STATUS_APPROVED.equals(status)) {
            return "已通过";
        }
        if (SellerAuth.STATUS_REJECTED.equals(status)) {
            return "已驳回";
        }
        if (SellerAuth.STATUS_REVOKED.equals(status)) {
            return "已撤回";
        }
        return "未知状态";
    }
}
