package com.zhuanzhuan.constant;

/**
 * 卖家认证状态
 */
public class SellerAuthStatusConstant {

    private SellerAuthStatusConstant() {
    }

    /**
     * 待审核
     */
    public static final Integer PENDING = 0;

    /**
     * 通过
     */
    public static final Integer APPROVED = 1;

    /**
     * 驳回
     */
    public static final Integer REJECTED = 2;

    /**
     * 撤回
     */
    public static final Integer REVOKED = 3;
}
