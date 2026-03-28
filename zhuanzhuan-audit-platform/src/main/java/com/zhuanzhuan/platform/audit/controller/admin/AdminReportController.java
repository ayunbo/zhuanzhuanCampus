package com.zhuanzhuan.platform.audit.controller.admin;

import com.zhuanzhuan.dto.AdminReportPageQueryDTO;
import com.zhuanzhuan.dto.ReportHandleDTO;
import com.zhuanzhuan.platform.audit.service.ReportService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.ReportDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端举报管理控制器。
 */
@RestController
@RequestMapping("/admin/report")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 分页查询举报列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> page(AdminReportPageQueryDTO dto) {
        return Result.success(reportService.pageQuery(dto));
    }

    /**
     * 查询举报详情。
     *
     * @param id 举报 ID
     * @return 举报详情
     */
    @GetMapping("/{id}")
    public Result<ReportDetailVO> detail(@PathVariable Long id) {
        return Result.success(reportService.getDetail(id));
    }

    /**
     * 处理举报。
     *
     * @param id 举报 ID
     * @param dto 处理信息
     * @return 操作结果
     */
    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestBody ReportHandleDTO dto) {
        reportService.handleReport(id, dto);
        return Result.success();
    }

    /**
     * 忽略举报。
     *
     * @param id 举报 ID
     * @return 操作结果
     */
    @PutMapping("/{id}/ignore")
    public Result<Void> ignore(@PathVariable Long id) {
        reportService.ignoreReport(id);
        return Result.success();
    }
}
