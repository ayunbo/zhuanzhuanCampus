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

            // 2、从方法参数中提取操作对象 ID（第一个 Long 类型参数）
            Long targetId = extractTargetId(joinPoint.getArgs());

            // 3、获取当前登录管理员信息
            Long adminId = BaseContext.getCurrentId();
            Admin currentAdmin = adminId == null ? null : adminMapper.getById(adminId);

            // 4、从方法参数中提取操作动作和详情
            String action = extractAction(joinPoint.getArgs(), operationType);
            String detail = extractDetail(joinPoint.getArgs());

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
     * 从方法参数中提取第一个 Long 类型的参数作为操作对象 ID。
     */
    private Long extractTargetId(Object[] args) {
        if (args == null) return null;
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
    private String extractAction(Object[] args, int operationType) {
        // 尝试从 DTO 参数中提取 status 字段来判断动作
        if (args != null) {
            for (Object arg : args) {
                if (arg != null) {
                    try {
                        java.lang.reflect.Method getStatus = arg.getClass().getMethod("getStatus");
                        Object status = getStatus.invoke(arg);
                        if (status instanceof Integer) {
                            return statusToAction(operationType, (Integer) status);
                        }
                    } catch (Exception ignored) {
                        // 该参数没有 getStatus 方法，跳过
                    }
                }
            }
        }
        return "操作";
    }

    /**
     * 将状态码转换为操作动作描述。
     */
    private String statusToAction(int operationType, int status) {
        return switch (operationType) {
            case 1 -> // 商品审核
                    status == 3 ? "通过" : status == 2 ? "驳回" : "审核";
            case 2 -> // 卖家认证审核
                    status == 1 ? "通过" : status == 2 ? "驳回" : "审核";
            case 3 -> // 举报处理
                    status == 1 ? "处理" : status == 2 ? "忽略" : "处理";
            default -> "操作";
        };
    }

    /**
     * 从方法参数中提取 reason 或 handleResult 字段作为操作详情。
     */
    private String extractDetail(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg != null) {
                // 尝试获取 reason 字段
                try {
                    java.lang.reflect.Method getReason = arg.getClass().getMethod("getReason");
                    Object reason = getReason.invoke(arg);
                    if (reason instanceof String && !((String) reason).isEmpty()) {
                        return (String) reason;
                    }
                } catch (Exception ignored) {
                }
                // 尝试获取 handleResult 字段
                try {
                    java.lang.reflect.Method getHandleResult = arg.getClass().getMethod("getHandleResult");
                    Object handleResult = getHandleResult.invoke(arg);
                    if (handleResult instanceof String && !((String) handleResult).isEmpty()) {
                        return (String) handleResult;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}
