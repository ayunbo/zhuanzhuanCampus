package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核操作流水实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /** 主键id。 */
    private Long id;

    /** 操作管理员id。 */
    private Long adminId;

    /** 管理员名称。 */
    private String adminName;

    /** 操作类型：1商品审核 2卖家认证审核 3举报处理。 */
    private Integer operationType;

    /** 操作对象id。 */
    private Long targetId;

    /** 操作动作：如通过/驳回/处理/忽略。 */
    private String action;

    /** 操作详情/备注。 */
    private String detail;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 创建人id。 */
    private Long createUser;

    /** 修改人id。 */
    private Long updateUser;
}
