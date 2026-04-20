package com.zhuanzhuan.platform.audit.controller.user;

import com.zhuanzhuan.dto.ReportSubmitDTO;
import com.zhuanzhuan.platform.audit.service.ReportService;
import com.zhuanzhuan.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端举报控制器。
 */
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
    @PostMapping
    public Result<Void> submit(@RequestBody ReportSubmitDTO dto) {
        reportService.submitReport(dto);
        return Result.success();
    }
}
