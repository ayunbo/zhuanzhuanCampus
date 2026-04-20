package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 管理员分页查询审核流水请求对象。
 */
@Data
public class AdminAuditLogPageQueryDTO {

    /** 操作类型：1商品审核 2卖家认证审核 3举报处理。 */
    private Integer operationType;

    /** 当前页码。 */
    private Integer page = 1;

    /** 每页记录数。 */
    private Integer pageSize = 10;
}
