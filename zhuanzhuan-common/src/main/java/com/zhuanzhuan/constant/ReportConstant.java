package com.zhuanzhuan.constant;

/**
 * 举报相关常量。
 */
public class ReportConstant {

    /** 处理状态：待处理。 */
    public static final int STATUS_PENDING = 0;

    /** 处理状态：已处理。 */
    public static final int STATUS_HANDLED = 1;

    /** 处理状态：已忽略。 */
    public static final int STATUS_IGNORED = 2;

    /** 举报对象类型：商品。 */
    public static final int TARGET_TYPE_GOODS = 1;

    /** 举报对象类型：用户。 */
    public static final int TARGET_TYPE_USER = 2;

    /** 举报对象类型：消息。 */
    public static final int TARGET_TYPE_MESSAGE = 3;

    /** 处理方式：仅标记已处理。 */
    public static final int ACTION_MARK_HANDLED = 1;

    /** 处理方式：下架商品。 */
    public static final int ACTION_OFF_SHELF_GOODS = 2;

    /** 处理方式：封禁用户。 */
    public static final int ACTION_BAN_USER = 3;
}
