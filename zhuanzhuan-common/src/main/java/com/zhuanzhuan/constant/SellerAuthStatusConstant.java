package com.zhuanzhuan.constant;

/**
 * 卖家认证状态常量
 */
public final class SellerAuthStatusConstant {

    private SellerAuthStatusConstant() {
    }

    public static final Integer PENDING = 0;
    public static final Integer APPROVED = 1;
    public static final Integer REJECTED = 2;
    public static final Integer REVOKED = 3;

    public static final String PENDING_DESC = "待审核";
    public static final String APPROVED_DESC = "已通过";
    public static final String REJECTED_DESC = "已驳回";
    public static final String REVOKED_DESC = "已撤回";
    public static final String UNKNOWN_DESC = "未知状态";
}
