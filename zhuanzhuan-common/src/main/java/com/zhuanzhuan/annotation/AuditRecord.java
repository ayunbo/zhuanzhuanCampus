package com.zhuanzhuan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审核流水记录注解。
 * 标注在需要自动记录审核操作流水的 Service 方法上，
 * 由 AuditLogAspect 切面拦截并自动写入 audit_log 表。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditRecord {

    /**
     * 操作类型：1商品审核 2卖家认证审核 3举报处理。
     */
    int operationType();

    /**
     * 操作对象 ID 所在字段名。
     * 当业务方法没有直接传入 Long 类型主键参数时，可通过该字段从 DTO 中提取目标对象主键。
     */
    String targetIdField() default "";

    /**
     * 操作动作来源字段名。
     * 适用于通过状态码、动作码等字段推导“通过/驳回/封禁”等动作描述。
     */
    String actionField() default "";

    /**
     * 操作详情来源字段名。
     * 常用于读取 reason、handleResult 等文本说明。
     */
    String detailField() default "";

    /**
     * 固定动作描述。
     * 配置后优先级高于 actionField，可用于“忽略举报”等固定动作场景。
     */
    String fixedAction() default "";

    /**
     * 固定详情描述。
     * 适用于业务方法参数中没有详情字段，但日志需要记录固定说明的场景。
     */
    String fixedDetail() default "";
}
