package com.zhuanzhuan.platform.audit.aspect;

import com.zhuanzhuan.annotation.AuditRecord;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.entity.Admin;
import com.zhuanzhuan.entity.AuditLog;
import com.zhuanzhuan.platform.account.mapper.AdminMapper;
import com.zhuanzhuan.platform.audit.mapper.AuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
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

    /**
     * 切入点：匹配所有标注了 @AuditRecord 注解的方法。
     */
    @Pointcut("@annotation(com.zhuanzhuan.annotation.AuditRecord)")
    public void auditRecordPointCut() {
    }

    /**
     * 后置通知：方法成功执行后自动记录审核流水。
     *
     * @param joinPoint 连接点
     */
    @AfterReturning("auditRecordPointCut()")
    public void recordAuditLog(JoinPoint joinPoint) {
        try {
            // 1、获取方法上的 @AuditRecord 注解，提取操作类型
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            AuditRecord auditRecord = signature.getMethod().getAnnotation(AuditRecord.class);
            int operationType = auditRecord.operationType();

            // 2、从方法参数中提取操作对象 ID（优先使用注解显式字段，其次回退到 Long 类型参数）
            Long targetId = extractTargetId(joinPoint.getArgs(), auditRecord);

            // 3、获取当前登录管理员信息
            Long adminId = BaseContext.getCurrentId();
            Admin currentAdmin = adminId == null ? null : adminMapper.getById(adminId);

            // 4、从方法参数中提取操作动作和详情
            String action = extractAction(joinPoint.getArgs(), auditRecord, operationType);
            String detail = extractDetail(joinPoint.getArgs(), auditRecord);

            // 5、构建审核流水实体并写入数据库
            AuditLog auditLog = AuditLog.builder()
                    .adminId(adminId != null ? adminId : 0L)
                    .adminName(currentAdmin != null ? currentAdmin.getName() : "")
                    .operationType(operationType)
                    .targetId(targetId != null ? targetId : 0L)
                    .action(action)
                    .detail(detail)
                    .build();

            auditLogMapper.insert(auditLog);
            log.info("审核流水已记录：operationType={}, targetId={}, action={}", operationType, targetId, action);

        } catch (Exception e) {
            // 审核流水记录失败不应影响主业务，仅打印日志
            log.error("记录审核流水失败", e);
        }
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
}
