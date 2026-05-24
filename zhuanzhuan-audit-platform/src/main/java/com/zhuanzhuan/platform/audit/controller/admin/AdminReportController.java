package com.zhuanzhuan.platform.audit.controller.admin;

import com.zhuanzhuan.dto.AdminReportPageQueryDTO;
import com.zhuanzhuan.dto.ReportHandleDTO;
import com.zhuanzhuan.platform.audit.service.ReportService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.ReportDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端举报管理控制器。
 * <p>
 * 管理员可查询用户提交的举报，查看举报对象详情，并进行处理或忽略。
 * 处理成功后会写入审核流水，保证举报处置过程可追溯。
 */
@Tag(name = "管理端举报处理接口")
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
    @Operation(summary = "分页查询举报列表", description = "支持按处理状态和举报对象类型筛选，供管理员查看待处理举报。")
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
    @Operation(summary = "查看举报详情", description = "返回举报人、举报对象、举报原因、处理结果和处理管理员等信息。")
    @GetMapping("/{id:\\d+}")
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
    @Operation(summary = "处理举报", description = "可仅标记已处理，也可联动下架被举报商品或封禁被举报用户；处理成功后记录审核流水。")
    @PutMapping("/{id:\\d+}/handle")
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
    @Operation(summary = "忽略举报", description = "将待处理举报标记为已忽略，并记录审核流水。")
    @PutMapping("/{id:\\d+}/ignore")
    public Result<Void> ignore(@PathVariable Long id) {
        reportService.ignoreReport(id);
        return Result.success();
    }
}
