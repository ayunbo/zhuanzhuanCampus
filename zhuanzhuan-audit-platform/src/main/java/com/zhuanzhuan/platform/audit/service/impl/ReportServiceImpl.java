package com.zhuanzhuan.platform.audit.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zhuanzhuan.annotation.AuditRecord;
import com.zhuanzhuan.constant.AuditOperationConstant;
import com.zhuanzhuan.constant.GoodsConstant;
import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.constant.ReportConstant;
import com.zhuanzhuan.constant.UserStatusConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.AdminReportPageQueryDTO;
import com.zhuanzhuan.dto.ReportHandleDTO;
import com.zhuanzhuan.dto.ReportSubmitDTO;
import com.zhuanzhuan.dto.UserReportPageQueryDTO;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.entity.Report;
import com.zhuanzhuan.entity.User;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.platform.account.mapper.UserMapper;
import com.zhuanzhuan.platform.audit.mapper.ReportMapper;
import com.zhuanzhuan.platform.audit.service.ReportService;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.service.RiskControlService;
import com.zhuanzhuan.service.notify.NoticePublishService;
import com.zhuanzhuan.vo.ReportDetailVO;
import com.zhuanzhuan.vo.ReportVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报业务实现。
 * <p>
 * 该服务覆盖举报闭环中的三类能力：
 * 1. 用户提交举报，并校验举报对象真实存在且当前用户有权举报；
 * 2. 用户查看本人举报记录和处理结果，防止越权查看他人举报；
 * 3. 管理员查看、处理、忽略举报，并在处理成功后记录审核流水。
 */
@Service
public class ReportServiceImpl implements ReportService {

    /** 举报原因和处理结果字段在数据库中均为 varchar(255)。 */
    private static final int MAX_REPORT_TEXT_LENGTH = 255;

    /** 分页查询单页最大条数，防止一次查询过多数据影响后台稳定性。 */
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RiskControlService riskControlService;

    @Autowired
    private NoticePublishService noticePublishService;

    /**
     * 用户提交举报。
     *
     * @param dto 举报信息
     */
    @Override
    public void submitReport(ReportSubmitDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        // 先校验参数、对象存在性和业务规则，避免无效举报进入数据库。
        validateSubmitDTO(dto);
        validateReportTargetVisible(dto, userId);
        if (reportMapper.countPendingByReporterAndTarget(userId, dto.getTargetType(), dto.getTargetId()) > 0) {
            throw new BaseException(MessageConstant.DUPLICATE_SUBMIT);
        }

        // 限制同一用户短时间内提交举报的频率，避免恶意刷举报工单。
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
            Report report = new Report();
            BeanUtils.copyProperties(dto, report);
            report.setReportUserId(userId);
            report.setStatus(ReportConstant.STATUS_PENDING);
            report.setReason(dto.getReason().trim());

            // create_time、create_user 等通用字段由 AutoFill 切面填充。
            reportMapper.insert(report);
            success = true;
        } finally {
            if (!success) {
                riskControlService.releaseDedupLock(dedupKey);
            }
        }
    }

    /**
     * 当前登录用户分页查询本人提交的举报记录。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageCurrentUserReports(UserReportPageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }

        UserReportPageQueryDTO queryDTO = normalizeUserPageDTO(dto);
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<ReportVO> records = reportMapper.pageQueryByUserId(userId, queryDTO);
        Page<ReportVO> pageInfo = (Page<ReportVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 当前登录用户查看本人提交的举报详情。
     *
     * @param id 举报 ID
     * @return 举报详情
     */
    @Override
    public ReportDetailVO getCurrentUserReportDetail(Long id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new UserNotLoginException(MessageConstant.USER_NOT_LOGIN);
        }
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        ReportDetailVO detailVO = reportMapper.selectDetailByIdAndReportUserId(id, userId);
        if (detailVO == null) {
            throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
        }
        return detailVO;
    }

    /**
     * 管理员分页查询举报列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(AdminReportPageQueryDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }

        AdminReportPageQueryDTO queryDTO = normalizeAdminPageDTO(dto);
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<ReportVO> records = reportMapper.pageQuery(queryDTO);
        Page<ReportVO> pageInfo = (Page<ReportVO>) records;
        return new PageResult(pageInfo.getTotal(), records);
    }

    /**
     * 管理员查询举报详情。
     *
     * @param id 举报 ID
     * @return 举报详情
     */
    @Override
    public ReportDetailVO getDetail(Long id) {
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) {
            throw new UserNotLoginException(MessageConstant.ADMIN_NOT_LOGIN);
        }
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        ReportDetailVO detailVO = reportMapper.selectDetailById(id);
        if (detailVO == null) {
            throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
        }
        return detailVO;
    }

    /**
     * 管理员处理举报。
     * <p>
     * 处理方式支持：
     * 1. 仅标记已处理；
     * 2. 对商品举报联动下架商品；
     * 3. 对用户举报联动封禁用户。
     * 举报记录更新和联动处罚处于同一事务中，任一环节失败都会回滚。
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
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }
        validateHandleDTO(dto);

        Long adminId = BaseContext.getCurrentId();
        checkAdminAuditRisk(adminId, "dedup:report-handle:" + id);
        boolean success = false;
        try {
            Report currentReport = reportMapper.selectById(id);
            if (currentReport == null) {
                throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
            }
            if (!Integer.valueOf(ReportConstant.STATUS_PENDING).equals(currentReport.getStatus())) {
                throw new BaseException(MessageConstant.REPORT_ALREADY_HANDLED);
            }

            Report updateReport = new Report();
            updateReport.setId(id);
            updateReport.setStatus(ReportConstant.STATUS_HANDLED);
            updateReport.setHandleAdminId(adminId);
            updateReport.setHandleResult(dto.getHandleResult());
            updateReport.setHandleTime(LocalDateTime.now());

            int rows = reportMapper.updateHandleResult(updateReport);
            if (rows <= 0) {
                throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
            }

            // 根据处理方式执行联动处罚。若联动失败，事务会回滚举报处理结果。
            handleLinkedPenalty(currentReport, dto, adminId);
            noticePublishService.publishReportResult(
                    currentReport.getReportUserId(),
                    id,
                    "举报处理结果",
                    truncate("你的举报已处理，处理结果：" + dto.getHandleResult(), 500)
            );
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
        if (id == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Long adminId = BaseContext.getCurrentId();
        checkAdminAuditRisk(adminId, "dedup:report-handle:" + id);
        boolean success = false;
        try {
            Report currentReport = reportMapper.selectById(id);
            if (currentReport == null) {
                throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
            }
            if (!Integer.valueOf(ReportConstant.STATUS_PENDING).equals(currentReport.getStatus())) {
                throw new BaseException(MessageConstant.REPORT_ALREADY_HANDLED);
            }

            Report updateReport = new Report();
            updateReport.setId(id);
            updateReport.setStatus(ReportConstant.STATUS_IGNORED);
            updateReport.setHandleAdminId(adminId);
            updateReport.setHandleResult("管理员忽略该举报");
            updateReport.setHandleTime(LocalDateTime.now());

            int rows = reportMapper.updateHandleResult(updateReport);
            if (rows <= 0) {
                throw new BaseException(MessageConstant.REPORT_NOT_FOUND);
            }
            noticePublishService.publishReportResult(
                    currentReport.getReportUserId(),
                    id,
                    "举报处理结果",
                    "你的举报已由管理员审核，本次暂不处理。"
            );
            success = true;
        } finally {
            if (!success) {
                riskControlService.releaseDedupLock("dedup:report-handle:" + id);
            }
        }
    }

    /**
     * 管理员审核类操作的基础风控校验。
     */
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

    /**
     * 根据管理员选择的处理方式执行联动处罚。
     */
    private void handleLinkedPenalty(Report report, ReportHandleDTO dto, Long adminId) {
        Integer action = dto.getHandleAction() == null
                ? ReportConstant.ACTION_MARK_HANDLED : dto.getHandleAction();
        if (Integer.valueOf(ReportConstant.ACTION_MARK_HANDLED).equals(action)) {
            return;
        }
        if (Integer.valueOf(ReportConstant.ACTION_OFF_SHELF_GOODS).equals(action)) {
            offShelfReportedGoods(report, dto, adminId);
            return;
        }
        if (Integer.valueOf(ReportConstant.ACTION_BAN_USER).equals(action)) {
            banReportedUser(report, adminId);
            return;
        }
        throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
    }

    /**
     * 对商品举报执行“下架商品”处罚。
     */
    private void offShelfReportedGoods(Report report, ReportHandleDTO dto, Long adminId) {
        if (!Integer.valueOf(ReportConstant.TARGET_TYPE_GOODS).equals(report.getTargetType())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }
        Goods currentGoods = goodsMapper.selectById(report.getTargetId());
        if (currentGoods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        if (GoodsConstant.STATUS_LOCKED.equals(currentGoods.getStatus())
                || GoodsConstant.STATUS_SOLD.equals(currentGoods.getStatus())) {
            throw new BaseException(MessageConstant.GOODS_AUDIT_STATUS_FLOW_INVALID);
        }
        if (GoodsConstant.STATUS_OFF_SHELF.equals(currentGoods.getStatus())) {
            return;
        }
        Goods updateGoods = Goods.builder()
                .id(currentGoods.getId())
                .status(GoodsConstant.STATUS_OFF_SHELF)
                .reason(dto.getHandleResult())
                .auditAdminId(adminId)
                .auditTime(LocalDateTime.now())
                .publishTime(currentGoods.getPublishTime())
                .lockOrderId(currentGoods.getLockOrderId())
                .version(currentGoods.getVersion())
                .build();
        int rows = goodsMapper.updateStatusById(updateGoods);
        if (rows <= 0) {
            throw new BaseException(MessageConstant.GOODS_OFF_SHELF_FAILED);
        }
        noticePublishService.publishGoodsAuditResult(
                currentGoods.getSellerId(),
                currentGoods.getId(),
                "商品已下架",
                truncate("你的商品因举报处理被平台下架，处理结果：" + dto.getHandleResult(), 500)
        );
    }

    /**
     * 对用户举报执行“封禁用户”处罚。
     */
    private void banReportedUser(Report report, Long adminId) {
        if (!Integer.valueOf(ReportConstant.TARGET_TYPE_USER).equals(report.getTargetType())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }
        User user = userMapper.getById(report.getTargetId());
        if (user == null) {
            throw new BaseException(MessageConstant.CURRENT_USER_NOT_FOUND);
        }
        if (UserStatusConstant.BANNED.equals(user.getStatus())) {
            return;
        }
        User updateUser = User.builder()
                .id(user.getId())
                .status(UserStatusConstant.BANNED)
                .updateTime(LocalDateTime.now())
                .updateUser(adminId)
                .build();
        userMapper.updateByIdSelective(updateUser);
        riskControlService.evictAuthStatus("user", user.getId());
        noticePublishService.publishReportResult(
                user.getId(),
                report.getId(),
                "账号处理通知",
                "你的账号因举报处理已被平台封禁，如有疑问请联系管理员。"
        );
    }

    /**
     * 校验举报提交参数。
     */
    private void validateSubmitDTO(ReportSubmitDTO dto) {
        if (dto == null || dto.getTargetType() == null || dto.getTargetId() == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }
        validateTargetType(dto.getTargetType());
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BaseException(MessageConstant.REPORT_REASON_REQUIRED);
        }
        if (dto.getReason().trim().length() > MAX_REPORT_TEXT_LENGTH) {
            throw new BaseException(MessageConstant.REPORT_REASON_TOO_LONG);
        }
    }

    /**
     * 校验举报对象是否真实存在，并确认当前用户有权举报该对象。
     */
    private void validateReportTargetVisible(ReportSubmitDTO dto, Long userId) {
        if (Integer.valueOf(ReportConstant.TARGET_TYPE_GOODS).equals(dto.getTargetType())) {
            Long sellerId = reportMapper.selectGoodsSellerId(dto.getTargetId());
            if (sellerId == null) {
                throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
            }
            if (sellerId.equals(userId)) {
                throw new BaseException(MessageConstant.REPORT_SELF_NOT_ALLOWED);
            }
            return;
        }

        if (Integer.valueOf(ReportConstant.TARGET_TYPE_USER).equals(dto.getTargetType())) {
            if (dto.getTargetId().equals(userId)) {
                throw new BaseException(MessageConstant.REPORT_SELF_NOT_ALLOWED);
            }
            if (reportMapper.countUserById(dto.getTargetId()) <= 0) {
                throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
            }
            return;
        }

        if (Integer.valueOf(ReportConstant.TARGET_TYPE_MESSAGE).equals(dto.getTargetType())) {
            if (reportMapper.countMessageVisibleToUser(dto.getTargetId(), userId) <= 0) {
                throw new BaseException(MessageConstant.REPORT_TARGET_NOT_FOUND);
            }
            return;
        }

        throw new BaseException(MessageConstant.REPORT_TARGET_TYPE_INVALID);
    }

    /**
     * 校验管理员处理举报参数。
     */
    private void validateHandleDTO(ReportHandleDTO dto) {
        if (dto == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }
        if (!StringUtils.hasText(dto.getHandleResult())) {
            throw new BaseException(MessageConstant.REPORT_HANDLE_RESULT_REQUIRED);
        }
        if (dto.getHandleResult().trim().length() > MAX_REPORT_TEXT_LENGTH) {
            throw new BaseException(MessageConstant.REPORT_HANDLE_RESULT_TOO_LONG);
        }
        if (dto.getHandleAction() != null
                && !Integer.valueOf(ReportConstant.ACTION_MARK_HANDLED).equals(dto.getHandleAction())
                && !Integer.valueOf(ReportConstant.ACTION_OFF_SHELF_GOODS).equals(dto.getHandleAction())
                && !Integer.valueOf(ReportConstant.ACTION_BAN_USER).equals(dto.getHandleAction())) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }
        dto.setHandleResult(dto.getHandleResult().trim());
    }

    private AdminReportPageQueryDTO normalizeAdminPageDTO(AdminReportPageQueryDTO dto) {
        AdminReportPageQueryDTO queryDTO = dto == null ? new AdminReportPageQueryDTO() : dto;
        normalizePage(queryDTO);
        validateOptionalReportStatus(queryDTO.getStatus());
        validateOptionalTargetType(queryDTO.getTargetType());
        return queryDTO;
    }

    private UserReportPageQueryDTO normalizeUserPageDTO(UserReportPageQueryDTO dto) {
        UserReportPageQueryDTO queryDTO = dto == null ? new UserReportPageQueryDTO() : dto;
        normalizePage(queryDTO);
        validateOptionalReportStatus(queryDTO.getStatus());
        validateOptionalTargetType(queryDTO.getTargetType());
        return queryDTO;
    }

    private void normalizePage(AdminReportPageQueryDTO dto) {
        if (dto.getPage() == null || dto.getPage() < 1) {
            dto.setPage(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
        if (dto.getPageSize() > MAX_PAGE_SIZE) {
            dto.setPageSize(MAX_PAGE_SIZE);
        }
    }

    private void normalizePage(UserReportPageQueryDTO dto) {
        if (dto.getPage() == null || dto.getPage() < 1) {
            dto.setPage(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
        if (dto.getPageSize() > MAX_PAGE_SIZE) {
            dto.setPageSize(MAX_PAGE_SIZE);
        }
    }

    private void validateOptionalReportStatus(Integer status) {
        if (status == null) {
            return;
        }
        if (!Integer.valueOf(ReportConstant.STATUS_PENDING).equals(status)
                && !Integer.valueOf(ReportConstant.STATUS_HANDLED).equals(status)
                && !Integer.valueOf(ReportConstant.STATUS_IGNORED).equals(status)) {
            throw new BaseException(MessageConstant.STATUS_PARAM_INVALID);
        }
    }

    private void validateOptionalTargetType(Integer targetType) {
        if (targetType != null) {
            validateTargetType(targetType);
        }
    }

    private void validateTargetType(Integer targetType) {
        if (!Integer.valueOf(ReportConstant.TARGET_TYPE_GOODS).equals(targetType)
                && !Integer.valueOf(ReportConstant.TARGET_TYPE_USER).equals(targetType)
                && !Integer.valueOf(ReportConstant.TARGET_TYPE_MESSAGE).equals(targetType)) {
            throw new BaseException(MessageConstant.REPORT_TARGET_TYPE_INVALID);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
