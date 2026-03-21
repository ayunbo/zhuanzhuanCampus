package com.zhuanzhuan.constant;

/**
 * 商品相关常量
 */
public final class GoodsConstant {

    private GoodsConstant() {
    }

    public static final Long DEFAULT_CATEGORY_ID = 0L;

    public static final Integer STATUS_DRAFT = 0;
    public static final Integer STATUS_PENDING_AUDIT = 1;
    public static final Integer STATUS_REJECTED = 2;
    public static final Integer STATUS_ON_SALE = 3;
    public static final Integer STATUS_LOCKED = 4;
    public static final Integer STATUS_SOLD = 5;
    public static final Integer STATUS_OFF_SHELF = 6;

    public static final String STATUS_DESC_DRAFT = "草稿";
    public static final String STATUS_DESC_PENDING_AUDIT = "待审核";
    public static final String STATUS_DESC_REJECTED = "已驳回";
    public static final String STATUS_DESC_ON_SALE = "在售";
    public static final String STATUS_DESC_LOCKED = "锁定";
    public static final String STATUS_DESC_SOLD = "已售出";
    public static final String STATUS_DESC_OFF_SHELF = "已下架";
    public static final String STATUS_DESC_UNKNOWN = "未知状态";

    public static final int INITIAL_VIEW_COUNT = 0;
    public static final int INITIAL_FAVORITE_COUNT = 0;
    public static final int INITIAL_VERSION = 0;
    public static final int DEFAULT_QUALITY = 5;
    public static final int MIN_QUALITY = 1;
    public static final int MAX_QUALITY = 5;
    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_LOCATION_LENGTH = 120;
    public static final int MAX_AUDIT_REASON_LENGTH = 255;
}
