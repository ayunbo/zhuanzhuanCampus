package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 管理员分页查询举报列表请求对象。
 */
@Data
public class AdminReportPageQueryDTO {

    /** 处理状态：0待处理 1已处理 2已忽略。 */
    private Integer status;

    /** 举报对象类型：1商品 2用户 3消息。 */
    private Integer targetType;

    /** 当前页码。 */
    private Integer page = 1;

    /** 每页记录数。 */
    private Integer pageSize = 10;
}
