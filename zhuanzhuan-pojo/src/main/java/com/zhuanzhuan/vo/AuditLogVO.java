package com.zhuanzhuan.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核操作流水视图对象。
 */
@Data
public class AuditLogVO {

    /** 流水id。 */
    private Long id;

    /** 管理员id。 */
    private Long adminId;

    /** 管理员名称。 */
    private String adminName;

    /** 操作类型：1商品审核 2卖家认证审核 3举报处理。 */
    private Integer operationType;

    /** 操作对象id。 */
    private Long targetId;

    /** 操作动作。 */
    private String action;

    /** 操作详情。 */
    private String detail;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
