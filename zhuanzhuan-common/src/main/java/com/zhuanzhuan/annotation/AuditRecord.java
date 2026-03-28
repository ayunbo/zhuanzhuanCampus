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
}
