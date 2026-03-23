package com.zhuanzhuan.platform.goods.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
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
    public void audit(Long goodsId, AdminGoodsAuditDTO dto) {
        // 1. 校验管理员登录信息和审核参数。
        Long currentAdminId = BaseContext.getCurrentId();
        if (currentAdminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }
        AdminGoodsAuditDTO auditDTO = dto == null ? new AdminGoodsAuditDTO() : dto;
        if (auditDTO.getStatus() == null) {
            throw new BaseException(MessageConstant.AUDIT_PARAM_INCOMPLETE);
        }

        // 2. 查询商品。
        Goods currentGoods = goodsMapper.selectById(goodsId);
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

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
    }
}
