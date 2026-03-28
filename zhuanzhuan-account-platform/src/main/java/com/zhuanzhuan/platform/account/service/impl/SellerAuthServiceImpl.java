package com.zhuanzhuan.platform.account.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.annotation.AuditRecord;
import com.zhuanzhuan.constant.AuditOperationConstant;
import com.zhuanzhuan.constant.MessageConstant;
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
     * 用户提交卖家认证申请。
     *
     * @param sellerAuthApplyDTO 认证申请信息
     */
    @Override
    public void submitSellerAuth(SellerAuthApplyDTO sellerAuthApplyDTO) {
        // 1、获取当前登录用户 ID，未登录则拒绝
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、查询当前用户，校验账号是否正常可用
        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (!UserStatusConstant.NORMAL.equals(currentUser.getStatus())) {
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3、校验当前角色是否允许发起卖家认证
        //    已是卖家的账号无需重复申请，其他非普通用户账号也不允许申请
        if (!RoleConstant.NORMAL_USER.equals(currentUser.getRole())) {
            if (RoleConstant.SELLER.equals(currentUser.getRole())) {
                throw new BaseException(MessageConstant.ALREADY_SELLER);
            }
            throw new BaseException(MessageConstant.ROLE_NOT_ALLOW_APPLY);
        }

        // 4、查询该用户最新一条认证记录，防止重复提交
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth != null && SellerAuthStatusConstant.PENDING.equals(latestAuth.getStatus())) {
            // 已有待审核申请，不允许再次提交
            throw new BaseException(MessageConstant.AUTH_ALREADY_PENDING);
        }
        if (latestAuth != null && SellerAuthStatusConstant.APPROVED.equals(latestAuth.getStatus())) {
            // 认证已通过，无需重复申请
            throw new BaseException(MessageConstant.AUTH_ALREADY_APPROVED);
        }

        // 5、将 DTO 属性拷贝到认证实体，补充系统生成字段
        SellerAuth sellerAuth = new SellerAuth();
        BeanUtils.copyProperties(sellerAuthApplyDTO, sellerAuth);
        sellerAuth.setUserId(userId);
        // 学号从当前登录用户档案中取，防止前端篡改
        sellerAuth.setStudentNo(currentUser.getStudentNo());
        sellerAuth.setStatus(SellerAuthStatusConstant.PENDING);

        // 6、执行数据库插入
        sellerAuthMapper.insert(sellerAuth);
    }

    /**
     * 查询当前用户最新的卖家认证结果。
     *
     * @return 认证结果视图对象
     */
    @Override
    public SellerAuthResultVO getCurrentSellerAuthResult() {
        // 1、获取当前登录用户 ID，未登录则拒绝
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、查询最新认证记录，不存在则提示暂无记录
        SellerAuth latestAuth = sellerAuthMapper.getLatestByUserId(userId);
        if (latestAuth == null) {
            throw new BaseException(MessageConstant.NO_SELLER_AUTH_RECORD);
        }

        // 3、将实体属性拷贝到 VO，并补充状态文案后返回
        SellerAuthResultVO vo = new SellerAuthResultVO();
        BeanUtils.copyProperties(latestAuth, vo);
        vo.setStatusDesc(toStatusDesc(latestAuth.getStatus()));
        return vo;
    }

    /**
     * 校验当前登录用户是否具有卖家角色。
     * 供其他服务调用，不满足则抛出业务异常。
     */
    @Override
    public void checkCurrentUserIsSeller() {
        // 1、获取当前登录用户 ID，未登录则拒绝
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 2、查询用户并校验角色，非卖家角色不允许访问卖家专属接口
        User currentUser = userMapper.getById(userId);
        if (currentUser == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (!RoleConstant.SELLER.equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.SELLER_ONLY);
        }
    }

    /**
     * 分页查询卖家认证申请列表（管理员侧）。
     *
     * @param pageQueryDTO 分页查询条件（page、pageSize 已有默认值）
     * @return 分页结果
     */
    @Override
    public PageResult pageQuerySellerAuth(AdminSellerAuthPageQueryDTO pageQueryDTO) {
        // 1、启动分页插件，page 和 pageSize 由 DTO 默认值保证非空
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 2、执行查询，PageHelper 自动拦截并追加 LIMIT
        List<SellerAuthResultVO> records = sellerAuthMapper.pageQuery(pageQueryDTO);
        Page<SellerAuthResultVO> pageInfo = (Page<SellerAuthResultVO>) records;

        // 3、为每条记录补充状态文案，方便前端直接展示
        for (SellerAuthResultVO record : records) {
            record.setStatusDesc(toStatusDesc(record.getStatus()));
        }

        // 4、封装总记录数和当前页数据返回
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 管理员审核卖家认证（通过/驳回）。
     * 涉及认证记录和用户角色两张表的写操作，使用事务保证一致性。
     *
     * @param sellerAuthAuditDTO 审核信息（authId、status、reason）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditRecord(operationType = AuditOperationConstant.SELLER_AUTH_AUDIT)
    public void auditSellerAuth(SellerAuthAuditDTO sellerAuthAuditDTO) {
        // 1、校验审核结果合法性，只允许通过或驳回两种状态
        Integer targetStatus = sellerAuthAuditDTO.getStatus();
        if (!SellerAuthStatusConstant.APPROVED.equals(targetStatus)
                && !SellerAuthStatusConstant.REJECTED.equals(targetStatus)) {
            throw new BaseException(MessageConstant.AUDIT_STATUS_INVALID);
        }

        // 2、驳回时必须填写驳回原因，便于用户了解未通过原因
        if (SellerAuthStatusConstant.REJECTED.equals(targetStatus)
                && !StringUtils.hasText(sellerAuthAuditDTO.getReason())) {
            throw new BaseException(MessageConstant.REJECT_REASON_REQUIRED);
        }

        // 3、查询待审核记录，确认申请存在且处于待审核状态
        SellerAuth sellerAuth = sellerAuthMapper.getById(sellerAuthAuditDTO.getAuthId());
        if (sellerAuth == null) {
            throw new BaseException(MessageConstant.AUTH_NOT_FOUND);
        }
        if (!SellerAuthStatusConstant.PENDING.equals(sellerAuth.getStatus())) {
            // 申请已被处理，防止重复操作
            throw new BaseException(MessageConstant.AUTH_ALREADY_AUDITED);
        }

        // 4、获取当前登录管理员 ID，用于写入审核人信息
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        // 5、审核通过前额外校验申请人账号状态，被封禁账号不能升级为卖家
        if (SellerAuthStatusConstant.APPROVED.equals(targetStatus)) {
            User authUser = userMapper.getById(sellerAuth.getUserId());
            if (authUser == null) {
                throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
            }
            if (!UserStatusConstant.NORMAL.equals(authUser.getStatus())) {
                throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
            }
        }

        // 6、更新认证记录：写入审核结果、审核人、审核时间，驳回时附带原因
        SellerAuth updateEntity = new SellerAuth();
        updateEntity.setId(sellerAuthAuditDTO.getAuthId());
        updateEntity.setStatus(targetStatus);
        updateEntity.setReason(SellerAuthStatusConstant.REJECTED.equals(targetStatus)
                ? sellerAuthAuditDTO.getReason() : null);
        updateEntity.setAuditAdminId(adminId);
        updateEntity.setAuditTime(LocalDateTime.now());
        sellerAuthMapper.updateAuditById(updateEntity);

        // 7、审核通过后同步将用户角色升级为卖家，卖家权限从此刻生效
        if (SellerAuthStatusConstant.APPROVED.equals(targetStatus)) {
            userMapper.updateRoleById(sellerAuth.getUserId(), RoleConstant.SELLER);
        }
    }

    /**
     * 将卖家认证状态码转换为中文描述文案。
     *
     * @param status 状态码
     * @return 对应的中文描述
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
