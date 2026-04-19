package com.zhuanzhuan.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核日志详情返回对象。
 */
@Data
public class AuditLogDetailVO {

    private Long id;
    private Long adminId;
    private String adminName;
    private Integer operationType;
    private String operationTypeDesc;
    private Long targetId;
    private String targetSummary;
    private String action;
    private String detail;
    private LocalDateTime createTime;
    private AdminGoodsDetailVO goodsDetail;
    private AuditSellerAuthDetailVO sellerAuthDetail;
    private ReportDetailVO reportDetail;
}
