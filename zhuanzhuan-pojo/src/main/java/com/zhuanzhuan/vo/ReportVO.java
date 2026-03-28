package com.zhuanzhuan.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报列表视图对象。
 */
@Data
public class ReportVO {

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

    /** 举报原因。 */
    private String reason;

    /** 处理状态：0待处理 1已处理 2已忽略。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
