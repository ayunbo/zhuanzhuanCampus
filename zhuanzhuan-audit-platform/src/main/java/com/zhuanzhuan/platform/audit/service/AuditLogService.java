package com.zhuanzhuan.platform.audit.service;

import com.zhuanzhuan.dto.AdminAuditLogPageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.AuditLogDetailVO;

/**
 * 审核流水业务接口。
 */
public interface AuditLogService {

    /**
     * 管理员分页查询审核流水。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(AdminAuditLogPageQueryDTO dto);

    /**
     * 查询审核流水详情。
     *
     * @param id 日志 ID
     * @return 日志详情
     */
    AuditLogDetailVO getDetail(Long id);
}
