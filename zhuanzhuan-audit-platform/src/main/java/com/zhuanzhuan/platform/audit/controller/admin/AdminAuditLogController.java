package com.zhuanzhuan.platform.audit.controller.admin;

import com.zhuanzhuan.dto.AdminAuditLogPageQueryDTO;
import com.zhuanzhuan.platform.audit.service.AuditLogService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端审核流水控制器。
 */
@RestController
@RequestMapping("/admin/audit-log")
public class AdminAuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 分页查询审核操作流水。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> page(AdminAuditLogPageQueryDTO dto) {
        return Result.success(auditLogService.pageQuery(dto));
    }
}
