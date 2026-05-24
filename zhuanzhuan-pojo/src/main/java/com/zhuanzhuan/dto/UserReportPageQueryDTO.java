package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 用户端分页查询本人举报记录请求对象。
 * <p>
 * 用户只能查看自己提交的举报记录，reportUserId 由后端从登录态中获取，
 * 前端不需要也不允许传入，避免越权查询其他用户的举报信息。
 */
@Data
public class UserReportPageQueryDTO {

    /** 处理状态：0待处理 1已处理 2已忽略；为空时查询全部。 */
    private Integer status;

    /** 举报对象类型：1商品 2用户 3消息；为空时查询全部。 */
    private Integer targetType;

    /** 当前页码，默认第 1 页。 */
    private Integer page = 1;

    /** 每页记录数，默认 10 条。 */
    private Integer pageSize = 10;
}
