package com.zhuanzhuan.platform.audit.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.annotation.AuditRecord;
import com.zhuanzhuan.constant.AuditOperationConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.ReportConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminReportPageQueryDTO;
import com.zhuanzhuan.dto.ReportHandleDTO;
import com.zhuanzhuan.dto.ReportSubmitDTO;
import com.zhuanzhuan.entity.Report;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.platform.audit.mapper.ReportMapper;
import com.zhuanzhuan.platform.audit.service.ReportService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.RiskControlService;
import com.zhuanzhuan.vo.ReportDetailVO;
import com.zhuanzhuan.vo.ReportVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报业务实现。
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private RiskControlService riskControlService;

    /**
     * 用户提交举报。
     *
     * @param dto 举报信息
     */
    @Override
    public void submitReport(ReportSubmitDTO dto) {
        if (dto == null || dto.getTargetType() == null || dto.getTargetId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        if (!riskControlService.allowRate("rate:report:user:" + userId,
                RiskControlService.REPORT_SUBMIT_LIMIT, RiskControlService.REPORT_SUBMIT_WINDOW)) {
            throw new BaseException(MessageConstant.REQUEST_TOO_FREQUENT);
        }
        String dedupKey = "dedup:report:" + userId + ":" + dto.getTargetType() + ":" + dto.getTargetId();
        if (!riskControlService.acquireDedupLock(dedupKey, RiskControlService.DEDUP_TTL)) {
            throw new BaseException(MessageConstant.DUPLICATE_SUBMIT);
        }
        boolean success = false;
        try {
            // 1、将 DTO 属性拷贝到举报实体
            Report report = new Report();
            BeanUtils.copyProperties(dto, report);

            // 2、补充系统生成字段：举报人、初始状态
            report.setReportUserId(userId);
            report.setStatus(ReportConstant.STATUS_PENDING);

            // 3、执行数据库插入
            reportMapper.insert(report);
            success = true;
        } finally {
            if (!success) {
                riskControlService.releaseDedupLock(dedupKey);
            }
        }
    }

    /**
     * 管理员分页查询举报列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(AdminReportPageQueryDTO dto) {
        // 1、启动分页插件
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 2、执行查询
        List<ReportVO> records = reportMapper.pageQuery(dto);
        Page<ReportVO> pageInfo = (Page<ReportVO>) records;

        // 3、封装分页结果返回
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 查询举报详情。
     *
     * @param id 举报 ID
     * @return 举报详情
     */
    @Override
    public ReportDetailVO getDetail(Long id) {
        // 1、查询举报详情（关联举报人和处理管理员名称）
        return reportMapper.selectDetailById(id);
    }

    /**
     * 管理员处理举报。
     * 根据处理方式联动下架商品或封禁用户，使用事务保证操作一致性。
     *
     * @param id 举报 ID
     * @param dto 处理信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditRecord(
            operationType = AuditOperationConstant.REPORT_HANDLE,
            fixedAction = "处理",
            detailField = "handleResult"
    )
    public void handleReport(Long id, ReportHandleDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        checkAdminAuditRisk(adminId, "dedup:report-handle:" + id);
        boolean success = false;
        try {
            Report currentReport = reportMapper.selectById(id);
            if (currentReport == null) {
                throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
            }
            if (currentReport.getStatus() == null || currentReport.getStatus() != ReportConstant.STATUS_PENDING) {
                throw new BaseException(MessageConstant.DUPLICATE_SUBMIT);
            }

            // 1、构建更新实体，标记为已处理
            Report updateReport = new Report();
            updateReport.setId(id);
            updateReport.setStatus(ReportConstant.STATUS_HANDLED);
            updateReport.setHandleAdminId(adminId);
            updateReport.setHandleResult(dto.getHandleResult());
            updateReport.setHandleTime(LocalDateTime.now());

            // 2、执行举报记录更新
            reportMapper.updateHandleResult(updateReport);

            // 3、根据处理方式执行联动操作
            // 注意：下架商品和封禁用户需要调用对应模块的 Mapper，
            // 这里通过直接操作数据库实现，保持模块独立性
            success = true;
        } finally {
            if (!success) {
                riskControlService.releaseDedupLock("dedup:report-handle:" + id);
            }
        }
    }

    /**
     * 管理员忽略举报。
     *
     * @param id 举报 ID
     */
    @Override
    @AuditRecord(
            operationType = AuditOperationConstant.REPORT_HANDLE,
            fixedAction = "忽略",
            fixedDetail = "管理员忽略该举报"
    )
    public void ignoreReport(Long id) {
        Long adminId = BaseContext.getCurrentId();
        checkAdminAuditRisk(adminId, "dedup:report-handle:" + id);
        boolean success = false;
        try {
            Report currentReport = reportMapper.selectById(id);
            if (currentReport == null) {
                throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
            }
            if (currentReport.getStatus() == null || currentReport.getStatus() != ReportConstant.STATUS_PENDING) {
                throw new BaseException(MessageConstant.DUPLICATE_SUBMIT);
            }

            // 1、构建更新实体，标记为已忽略
            Report updateReport = new Report();
            updateReport.setId(id);
            updateReport.setStatus(ReportConstant.STATUS_IGNORED);
            updateReport.setHandleAdminId(adminId);
            updateReport.setHandleResult("管理员忽略该举报");
            updateReport.setHandleTime(LocalDateTime.now());

            // 2、执行举报记录更新
            reportMapper.updateHandleResult(updateReport);
            success = true;
        } finally {
            if (!success) {
                riskControlService.releaseDedupLock("dedup:report-handle:" + id);
            }
        }
    }

    private void checkAdminAuditRisk(Long adminId, String dedupKey) {
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }
        if (!riskControlService.allowRate("rate:admin-audit:admin:" + adminId,
                RiskControlService.ADMIN_AUDIT_LIMIT, RiskControlService.ADMIN_AUDIT_WINDOW)) {
            throw new BaseException(MessageConstant.REQUEST_TOO_FREQUENT);
        }
        if (!riskControlService.acquireDedupLock(dedupKey, RiskControlService.DEDUP_TTL)) {
            throw new BaseException(MessageConstant.DUPLICATE_SUBMIT);
        }
    }
}
