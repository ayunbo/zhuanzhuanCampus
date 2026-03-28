package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 用户提交举报请求对象。
 */
@Data
public class ReportSubmitDTO {

    /**
     * 举报对象类型：1商品 2用户 3消息。
     */
    private Integer targetType;

    /**
     * 举报对象 ID（商品ID / 用户ID / 消息ID）。
     */
    private Long targetId;

    /**
     * 举报原因。
     */
    private String reason;
}
