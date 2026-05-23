package com.zhuanzhuan.platform.goods.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.annotation.AuditRecord;
import com.zhuanzhuan.constant.AuditOperationConstant;
import com.zhuanzhuan.constant.GoodsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminGoodsAuditDTO;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.platform.goods.mapper.GoodsImageMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.platform.goods.service.AdminGoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.RiskControlService;
import com.zhuanzhuan.vo.AdminGoodsDetailVO;
import com.zhuanzhuan.vo.AdminGoodsPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端商品服务实现。
 */
@Service
public class AdminGoodsServiceImpl implements AdminGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsImageMapper goodsImageMapper;

    @Autowired
    private RiskControlService riskControlService;

    /**
     * 分页查询商品。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult page(AdminGoodsPageQueryDTO dto) {
        // 1. 校验管理员登录信息。
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        // 2. 执行分页查询。
        AdminGoodsPageQueryDTO queryDTO = dto == null ? new AdminGoodsPageQueryDTO() : dto;
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<AdminGoodsPageVO> records = goodsMapper.pageAdmin(queryDTO);
        Page<AdminGoodsPageVO> pageInfo = (Page<AdminGoodsPageVO>) records;

        // 3. 返回分页结果。
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 查询商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    @Override
    public AdminGoodsDetailVO getDetail(Long goodsId) {
        // 1. 校验管理员登录信息。
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        // 2. 查询商品详情。
        AdminGoodsDetailVO detailVO = goodsMapper.detailAdmin(goodsId);
        if (detailVO == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        // 3. 补充图片列表。
        detailVO.setImages(goodsImageMapper.selectByGoodsId(goodsId));
        return detailVO;
    }

    /**
     * 审核商品。
     *
     * @param goodsId 商品 ID
     * @param dto 审核参数
     */
    @Override
    @Transactional
    @AuditRecord(
            operationType = AuditOperationConstant.GOODS_AUDIT,
            actionField = "status",
            detailField = "reason"
    )
    public void audit(Long goodsId, AdminGoodsAuditDTO dto) {
        // 1. 校验管理员登录信息和审核参数。
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }
        if (!riskControlService.allowRate("rate:admin-audit:admin:" + currentAdminId,
                RiskControlService.ADMIN_AUDIT_LIMIT, RiskControlService.ADMIN_AUDIT_WINDOW)) {
            throw new BaseException(MessageConstant.REQUEST_TOO_FREQUENT);
        }
        String dedupKey = "dedup:audit:goods:" + goodsId;
        if (!riskControlService.acquireDedupLock(dedupKey, RiskControlService.DEDUP_TTL)) {
            throw new BaseException(MessageConstant.DUPLICATE_SUBMIT);
        }
        boolean success = false;
        try {
            AdminGoodsAuditDTO auditDTO = dto == null ? new AdminGoodsAuditDTO() : dto;
            if (auditDTO.getStatus() == null) {
                throw new BaseException(MessageConstant.AUDIT_PARAM_INCOMPLETE);
            }
            validateAuditStatus(auditDTO.getStatus());
            validateRejectReason(auditDTO.getStatus(), auditDTO.getReason());

            // 2. 查询商品。
            Goods currentGoods = goodsMapper.selectById(goodsId);
            if (currentGoods == null) {
                throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
            }
            validateAuditFlow(currentGoods.getStatus());

            // 3. 更新审核结果。
            LocalDateTime now = LocalDateTime.now();
            Goods updateGoods = Goods.builder()
                    .id(goodsId)
                    .status(auditDTO.getStatus())
                    .reason(auditDTO.getReason())
                    .auditAdminId(currentAdminId)
                    .auditTime(now)
                    .publishTime(GoodsConstant.STATUS_ON_SALE.equals(auditDTO.getStatus()) ? now : null)
                    .lockOrderId(currentGoods.getLockOrderId())
                    .version(currentGoods.getVersion())
                    .build();
            int rows = goodsMapper.updateStatusById(updateGoods);
            if (rows <= 0) {
                throw new BaseException(MessageConstant.GOODS_AUDIT_FAILED);
            }
            success = true;
        } finally {
            if (!success) {
                riskControlService.releaseDedupLock(dedupKey);
            }
        }
    }

    private void validateAuditStatus(Integer status) {
        if (!GoodsConstant.STATUS_ON_SALE.equals(status)
                && !GoodsConstant.STATUS_REJECTED.equals(status)) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_STATUS_INVALID);
        }
    }

    private void validateAuditFlow(Integer currentStatus) {
        if (!GoodsConstant.STATUS_PENDING_AUDIT.equals(currentStatus)) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_STATUS_FLOW_INVALID);
        }
    }

    private void validateRejectReason(Integer status, String reason) {
        if (!GoodsConstant.STATUS_REJECTED.equals(status)) {
            return;
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BaseException(MessageConstant.REJECT_REASON_REQUIRED);
        }
        if (reason.length() > GoodsConstant.MAX_AUDIT_REASON_LENGTH) {
            throw new BaseException(MessageConstant.REJECT_REASON_TOO_LONG);
        }
    }

    /**
     * 管理员下架商品。
     * <p>
     * 该操作属于审核风控处理的一部分，常用于发现已上架商品存在违规、信息不实等问题时，
     * 将商品从用户端可见列表中移除。方法执行成功后会由审核流水切面记录“下架”动作，
     * 便于后续追溯是哪位管理员在什么时间处理了哪一个商品。
     *
     * @param goodsId 商品 ID
     */
    @Override
    @Transactional
    @AuditRecord(
            operationType = AuditOperationConstant.GOODS_AUDIT,
            fixedAction = "下架"
    )
    public void offShelf(Long goodsId) {
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        Goods currentGoods = goodsMapper.selectById(goodsId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        if (!GoodsConstant.STATUS_ON_SALE.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_OFF_SHELF_STATUS_INVALID);
        }

        Goods updateGoods = Goods.builder()
                .id(goodsId)
                .status(GoodsConstant.STATUS_OFF_SHELF)
                .reason(currentGoods.getReason())
                .auditAdminId(currentGoods.getAuditAdminId())
                .auditTime(currentGoods.getAuditTime())
                .publishTime(currentGoods.getPublishTime())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();
        int rows = goodsMapper.updateStatusById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_OFF_SHELF_FAILED);
        }
    }

    /**
     * 管理员删除商品。
     * <p>
     * 删除是更强的风控处置方式，会同时删除商品主记录和图片记录。由于商品被硬删除后无法再通过
     * 商品表查询详情，审核流水切面会在删除前抓取商品快照，并在删除成功后写入 audit_log.detail，
     * 避免出现“有删除记录但无法知道删除了什么商品”的追溯缺口。
     *
     * @param goodsId 商品 ID
     */
    @Override
    @Transactional
    @AuditRecord(
            operationType = AuditOperationConstant.GOODS_AUDIT,
            fixedAction = "删除"
    )
    public void delete(Long goodsId) {
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        Goods currentGoods = goodsMapper.selectById(goodsId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        if (GoodsConstant.STATUS_LOCKED.equals(currentGoods.getStatus())
                || GoodsConstant.STATUS_SOLD.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_DELETE_STATUS_INVALID);
        }

        goodsImageMapper.deleteByGoodsId(goodsId);
        int rows = goodsMapper.deleteById(goodsId);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_DELETE_FAILED);
        }
    }
}
