package com.zhuanzhuan.platform.audit.controller.admin;

import com.zhuanzhuan.dto.AdminAuditLogPageQueryDTO;
import com.zhuanzhuan.platform.audit.service.AuditLogService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.AuditLogDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * 查询审核流水详情。
     *
     * @param id 日志 ID
     * @return 日志详情
     */
    @GetMapping("/{id}")
    public Result<AuditLogDetailVO> detail(@PathVariable Long id) {
        return Result.success(auditLogService.getDetail(id));
    }
}
