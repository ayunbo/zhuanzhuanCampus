package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 管理员处理举报请求对象。
 */
@Data
public class ReportHandleDTO {

    /**
     * 处理结果描述。
     */
    private String handleResult;

    /**
     * 处理方式：1仅标记已处理 2下架商品 3封禁用户。
     */
    private Integer handleAction;
}
