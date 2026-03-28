package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 举报实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    /** 主键id。 */
    private Long id;

    /** 举报人id。 */
    private Long reportUserId;

    /** 举报对象类型：1商品 2用户 3消息。 */
    private Integer targetType;

    /** 举报对象id。 */
    private Long targetId;

    /** 举报原因。 */
    private String reason;

    /** 处理状态：0待处理 1已处理 2已忽略。 */
    private Integer status;

    /** 处理管理员id。 */
    private Long handleAdminId;

    /** 处理结果。 */
    private String handleResult;

    /** 处理时间。 */
    private LocalDateTime handleTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 创建人id。 */
    private Long createUser;

    /** 修改人id。 */
    private Long updateUser;
}
