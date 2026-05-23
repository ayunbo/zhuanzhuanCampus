package com.zhuanzhuan.vo;

import java.math.BigDecimal;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报详情视图对象。
 */
@Data
public class ReportDetailVO {

    /** 举报id。 */
    private Long id;

    /** 举报人id。 */
    private Long reportUserId;

    /** 举报人名称。 */
    private String reportUserName;

    /** 举报人学号。 */
    private String reportUserStudentNo;

    /** 举报人手机号。 */
    private String reportUserPhone;

    /** 举报对象类型：1商品 2用户 3消息。 */
    private Integer targetType;

    /** 举报对象id。 */
    private Long targetId;

    /** 举报对象名称（商品标题/用户昵称等）。 */
    private String targetName;

    /** 被举报用户/商家学号。 */
    private String targetUserStudentNo;

    /** 被举报用户/商家手机号。 */
    private String targetUserPhone;

    /** 被举报用户/商家角色。 */
    private Integer targetUserRole;

    /** 被举报用户/商家状态。 */
    private Integer targetUserStatus;

    /** 被举报用户/商家校区。 */
    private String targetUserCampus;

    /** 被举报用户/商家简介。 */
    private String targetUserIntro;

    /** 被举报商品标题。 */
    private String goodsTitle;

    /** 被举报商品价格。 */
    private BigDecimal goodsPrice;

    /** 被举报商品状态。 */
    private Integer goodsStatus;

    /** 被举报商品状态描述。 */
    private String goodsStatusDesc;

    /** 被举报商品分类名称。 */
    private String goodsCategoryName;

    /** 被举报商品面交地点。 */
    private String goodsLocation;

    /** 被举报商品描述。 */
    private String goodsDetail;

    /** 被举报商品封面。 */
    private String goodsCover;

    /** 被举报商品发布人 ID。 */
    private Long goodsSellerId;

    /** 被举报商品发布人昵称。 */
    private String goodsSellerName;

    /** 被举报商品发布人学号。 */
    private String goodsSellerStudentNo;

    /** 被举报商品发布人手机号。 */
    private String goodsSellerPhone;

    /** 被举报商品发布人校区。 */
    private String goodsSellerCampus;

    /** 举报原因。 */
    private String reason;

    /** 处理状态：0待处理 1已处理 2已忽略。 */
    private Integer status;

    /** 处理管理员id。 */
    private Long handleAdminId;

    /** 处理管理员名称。 */
    private String handleAdminName;

    /** 处理结果。 */
    private String handleResult;

    /** 处理时间。 */
    private LocalDateTime handleTime;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
