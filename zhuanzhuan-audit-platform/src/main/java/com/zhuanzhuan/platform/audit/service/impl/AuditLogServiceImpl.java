package com.zhuanzhuan.platform.audit.service.impl;

import com.zhuanzhuan.constant.AuditOperationConstant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.dto.AdminAuditLogPageQueryDTO;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.account.mapper.SellerAuthMapper;
import com.zhuanzhuan.platform.audit.mapper.AuditLogMapper;
import com.zhuanzhuan.platform.audit.mapper.ReportMapper;
import com.zhuanzhuan.platform.audit.service.AuditLogService;
import com.zhuanzhuan.platform.goods.mapper.GoodsImageMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.AdminGoodsDetailVO;
import com.zhuanzhuan.vo.AuditLogDetailVO;
import com.zhuanzhuan.vo.AuditLogVO;
import com.zhuanzhuan.vo.AuditSellerAuthDetailVO;
import com.zhuanzhuan.vo.ReportDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核流水业务实现。
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsImageMapper goodsImageMapper;

    @Autowired
    private SellerAuthMapper sellerAuthMapper;

    @Autowired
    private ReportMapper reportMapper;

    /**
     * 管理员分页查询审核流水。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(AdminAuditLogPageQueryDTO dto) {
        // 1、启动分页插件
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 2、执行查询
        List<AuditLogVO> records = auditLogMapper.pageQuery(dto);
        Page<AuditLogVO> pageInfo = (Page<AuditLogVO>) records;

        // 3、封装分页结果返回
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 查询审核日志详情。
     *
     * @param id 日志 ID
     * @return 日志详情
     */
    @Override
    public AuditLogDetailVO getDetail(Long id) {
        AuditLogVO baseLog = auditLogMapper.getById(id);
        if (baseLog == null) {
            throw new BaseException("日志不存在");
        }

        AuditLogDetailVO detailVO = new AuditLogDetailVO();
        BeanUtils.copyProperties(baseLog, detailVO);

        if (AuditOperationConstant.GOODS_AUDIT == baseLog.getOperationType()) {
            AdminGoodsDetailVO goodsDetail = goodsMapper.detailAdmin(baseLog.getTargetId());
            if (goodsDetail != null) {
                goodsDetail.setImages(goodsImageMapper.selectByGoodsId(baseLog.getTargetId()));
            }
            detailVO.setGoodsDetail(goodsDetail);
            return detailVO;
        }

        if (AuditOperationConstant.SELLER_AUTH_AUDIT == baseLog.getOperationType()) {
            AuditSellerAuthDetailVO sellerAuthDetail = sellerAuthMapper.selectDetailById(baseLog.getTargetId());
            detailVO.setSellerAuthDetail(sellerAuthDetail);
            return detailVO;
        }

        if (AuditOperationConstant.REPORT_HANDLE == baseLog.getOperationType()) {
            ReportDetailVO reportDetail = reportMapper.selectDetailById(baseLog.getTargetId());
            detailVO.setReportDetail(reportDetail);
        }

        return detailVO;
    }
}
