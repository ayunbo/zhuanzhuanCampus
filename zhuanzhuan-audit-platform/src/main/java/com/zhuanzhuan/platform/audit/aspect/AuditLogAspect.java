package com.zhuanzhuan.platform.audit.aspect;

import com.zhuanzhuan.annotation.AuditRecord;
import com.zhuanzhuan.constant.AuditOperationConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.entity.Admin;
import com.zhuanzhuan.entity.AuditLog;
import com.zhuanzhuan.platform.account.mapper.AdminMapper;
import com.zhuanzhuan.platform.audit.mapper.AuditLogMapper;
import com.zhuanzhuan.platform.goods.mapper.GoodsMapper;
import com.zhuanzhuan.vo.AdminGoodsDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 审核流水自动记录切面。
 * 拦截标注了 @AuditRecord 注解的方法，在方法成功返回后自动写入 audit_log 表，
 * 实现审核操作的"可追溯、可审计"。
 */
@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 切入点：匹配所有标注了 @AuditRecord 注解的方法。
     */
    @Pointcut("@annotation(com.zhuanzhuan.annotation.AuditRecord)")
    public void auditRecordPointCut() {
    }

    /**
     * 环绕通知：业务方法执行成功后自动记录审核流水。
     * <p>
     * 这里使用环绕通知而不是普通后置通知，是为了在“删除商品”这类硬删除操作执行前先抓取商品快照。
     * 如果等删除成功后再查商品表，商品详情已经不存在，日志只能留下商品 ID，无法满足审核风控的可追溯要求。
     *
     * @param joinPoint 连接点
     * @return 原业务方法返回值
     * @throws Throwable 原业务方法抛出的异常需要继续向外传播，保证事务回滚和错误响应不被吞掉
     */
    @Around("auditRecordPointCut()")
    public Object recordAuditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AuditRecord auditRecord = signature.getMethod().getAnnotation(AuditRecord.class);
        int operationType = auditRecord.operationType();
        Long targetId = extractTargetId(joinPoint.getArgs(), auditRecord);

        // 商品删除、下架等处置操作可能会改变或删除商品记录，因此在主业务执行前先保存关键快照。
        String snapshotDetail = buildSnapshotDetail(operationType, targetId);

        Object result = joinPoint.proceed();

        try {
            // 业务成功后再写流水，避免失败操作也被记录成已完成的审核动作。
            Long adminId = BaseContext.getCurrentId();
            Admin currentAdmin = adminId == null ? null : adminMapper.getById(adminId);
            String action = extractAction(joinPoint.getArgs(), auditRecord, operationType);
            String detail = extractDetail(joinPoint.getArgs(), auditRecord);
            if (!StringUtils.hasText(detail)) {
                detail = snapshotDetail;
            }

            AuditLog auditLog = AuditLog.builder()
                    .adminId(adminId != null ? adminId : 0L)
                    .adminName(currentAdmin != null ? currentAdmin.getName() : "")
                    .operationType(operationType)
                    .targetId(targetId != null ? targetId : 0L)
                    .action(action)
                    .detail(truncateDetail(detail))
                    .build();

            auditLogMapper.insert(auditLog);
            log.info("审核流水已记录：operationType={}, targetId={}, action={}", operationType, targetId, action);
        } catch (Exception e) {
            // 审核流水记录失败不应影响主业务，仅打印日志，避免因为日志异常导致审核操作回滚。
            log.error("记录审核流水失败", e);
        }

        return result;
    }

    /**
     * 从方法参数中提取操作对象 ID。
     * 优先使用注解声明的 targetIdField，从 DTO 中读取主键；
     * 若未声明，则回退到直接传入的 Long 类型参数。
     */
    private Long extractTargetId(Object[] args, AuditRecord auditRecord) {
        if (args == null) {
            return null;
        }

        if (StringUtils.hasText(auditRecord.targetIdField())) {
            Long targetId = toLong(extractPropertyFromArgs(args, auditRecord.targetIdField()));
            if (targetId != null) {
                return targetId;
            }
        }

        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }

    /**
     * 根据操作类型生成操作动作描述。
     */
    private String extractAction(Object[] args, AuditRecord auditRecord, int operationType) {
        if (StringUtils.hasText(auditRecord.fixedAction())) {
            return auditRecord.fixedAction();
        }

        if (StringUtils.hasText(auditRecord.actionField())) {
            String action = actionValueToText(operationType, extractPropertyFromArgs(args, auditRecord.actionField()));
            if (StringUtils.hasText(action)) {
                return action;
            }
        }

        if (args != null) {
            String action = actionValueToText(operationType, extractPropertyFromArgs(args, "status"));
            if (StringUtils.hasText(action)) {
                return action;
            }
        }
        return "操作";
    }

    /**
     * 将状态码转换为操作动作描述。
     */
    private String actionValueToText(int operationType, Object actionValue) {
        if (actionValue instanceof Number number) {
            return statusToAction(operationType, number.intValue());
        }
        if (actionValue instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        return null;
    }

    private String statusToAction(int operationType, int status) {
        return switch (operationType) {
            case 1 -> // 商品审核
                    status == 3 ? "通过" : status == 2 ? "驳回" : "审核";
            case 2 -> // 卖家认证审核
                    status == 1 ? "通过" : status == 2 ? "驳回" : "审核";
            case 3 -> // 举报处理
                    status == 2 ? "下架商品" : status == 3 ? "封禁用户" : "处理";
            default -> "操作";
        };
    }

    /**
     * 从方法参数中提取 reason 或 handleResult 字段作为操作详情。
     */
    private String extractDetail(Object[] args, AuditRecord auditRecord) {
        if (StringUtils.hasText(auditRecord.fixedDetail())) {
            return auditRecord.fixedDetail();
        }

        if (StringUtils.hasText(auditRecord.detailField())) {
            String detail = toText(extractPropertyFromArgs(args, auditRecord.detailField()));
            if (StringUtils.hasText(detail)) {
                return detail;
            }
        }

        String reason = toText(extractPropertyFromArgs(args, "reason"));
        if (StringUtils.hasText(reason)) {
            return reason;
        }

        String handleResult = toText(extractPropertyFromArgs(args, "handleResult"));
        if (StringUtils.hasText(handleResult)) {
            return handleResult;
        }

        return null;
    }

    private Object extractPropertyFromArgs(Object[] args, String propertyName) {
        if (args == null || !StringUtils.hasText(propertyName)) {
            return null;
        }
        for (Object arg : args) {
            Object value = extractProperty(arg, propertyName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Object extractProperty(Object target, String propertyName) {
        if (target == null || !StringUtils.hasText(propertyName)) {
            return null;
        }

        String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            return target.getClass().getMethod(getterName).invoke(target);
        } catch (Exception ignored) {
        }

        try {
            var field = target.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception ignored) {
        }

        return null;
    }

    private Long toLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String toText(Object value) {
        if (value instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        return null;
    }

    /**
     * 构建审核对象快照详情。
     * <p>
     * 目前重点补齐商品审核风控链路：商品被下架或删除后，日志仍应保留商品标题、卖家和处理前状态。
     * 其他操作类型暂不需要额外快照，继续使用注解指定的 reason、handleResult 等业务字段。
     */
    private String buildSnapshotDetail(int operationType, Long targetId) {
        if (operationType != AuditOperationConstant.GOODS_AUDIT || targetId == null) {
            return null;
        }

        try {
            AdminGoodsDetailVO goodsDetail = goodsMapper.detailAdmin(targetId);
            if (goodsDetail == null) {
                return null;
            }
            return "商品快照：ID=" + safeText(goodsDetail.getId())
                    + "；标题=" + safeText(goodsDetail.getTitle())
                    + "；卖家=" + safeText(goodsDetail.getSellerName())
                    + "；卖家学号=" + safeText(goodsDetail.getSellerStudentNo())
                    + "；分类=" + safeText(goodsDetail.getCategoryName())
                    + "；价格=" + safeText(goodsDetail.getPrice())
                    + "；处理前状态=" + safeText(goodsDetail.getStatusDesc());
        } catch (Exception e) {
            // 快照只是增强追溯信息，不能影响主审核业务。
            log.warn("构建商品审核快照失败，targetId={}", targetId, e);
            return null;
        }
    }

    /**
     * audit_log.detail 字段长度为 500，这里做统一截断，避免个别商品标题或详情过长导致插入失败。
     */
    private String truncateDetail(String detail) {
        if (detail == null || detail.length() <= 500) {
            return detail;
        }
        return detail.substring(0, 500);
    }

    private String safeText(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
