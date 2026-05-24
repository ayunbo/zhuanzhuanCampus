package com.zhuanzhuan.platform.audit.controller.user;

import com.zhuanzhuan.dto.ReportSubmitDTO;
import com.zhuanzhuan.dto.UserReportPageQueryDTO;
import com.zhuanzhuan.platform.audit.service.ReportService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.ReportDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端举报控制器。
 * <p>
 * 用户可在登录后提交举报，并查询本人提交过的举报及处理结果。
 * 所有查询都以后端登录态中的用户 ID 为准，不接收前端传入的 reportUserId，
 * 防止用户通过篡改参数查看他人的举报记录。
 */
@Tag(name = "用户端举报接口")
@RestController
@RequestMapping("/user/report")
public class UserReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 用户提交举报。
     *
     * @param dto 举报信息
     * @return 操作结果
     */
    @Operation(summary = "提交举报", description = "支持举报商品、用户或聊天消息；后端会校验举报对象存在性、举报原因和重复待处理举报。")
    @PostMapping
    public Result<Void> submit(@RequestBody ReportSubmitDTO dto) {
        reportService.submitReport(dto);
        return Result.success();
    }

    /**
     * 用户分页查询本人提交的举报记录。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询我的举报", description = "只返回当前登录用户提交的举报记录，可按处理状态和举报对象类型筛选。")
    @GetMapping("/page")
    public Result<PageResult> page(UserReportPageQueryDTO dto) {
        return Result.success(reportService.pageCurrentUserReports(dto));
    }

    /**
     * 用户查看本人提交的举报详情。
     *
     * @param id 举报 ID
     * @return 举报详情
     */
    @Operation(summary = "查看我的举报详情", description = "只允许查看当前登录用户自己的举报详情及处理结果。")
    @GetMapping("/{id:\\d+}")
    public Result<ReportDetailVO> detail(@PathVariable Long id) {
        return Result.success(reportService.getCurrentUserReportDetail(id));
    }
}
