package com.zhuanzhuan.vo;

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

    /** 举报对象类型：1商品 2用户 3消息。 */
    private Integer targetType;

    /** 举报对象id。 */
    private Long targetId;

    /** 举报对象名称（商品标题/用户昵称等）。 */
    private String targetName;

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
