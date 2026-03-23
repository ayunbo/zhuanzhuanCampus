package com.zhuanzhuan.controller.user.notify;

import com.zhuanzhuan.dto.notify.NoticeMessageQueryDTO;
import com.zhuanzhuan.dto.notify.NoticeReadDTO;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.notify.NoticeService;
import com.zhuanzhuan.vo.notify.NoticeMessageVO;
import com.zhuanzhuan.vo.notify.NoticeSessionSummaryVO;
import com.zhuanzhuan.vo.notify.NoticeUnreadCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户通知接口")
@RestController
@RequestMapping("/user/notice")
public class UserNoticeController {

    @Autowired
    private NoticeService noticeService;

    @Operation(summary = "查询系统通知会话摘要")
    @GetMapping("/session/summary")
    public Result<NoticeSessionSummaryVO> getSessionSummary() {
        return Result.success(noticeService.getSessionSummary());
    }

    @Operation(summary = "查询当前用户通知消息列表")
    @GetMapping("/message/list")
    public Result<List<NoticeMessageVO>> listMessages(NoticeMessageQueryDTO noticeMessageQueryDTO) {
        return Result.success(noticeService.listMessages(noticeMessageQueryDTO));
    }

    @Operation(summary = "将单条通知标记为已读")
    @PatchMapping("/read")
    public Result<Void> readNotice(@RequestBody NoticeReadDTO noticeReadDTO) {
        noticeService.readNotice(noticeReadDTO);
        return Result.success();
    }

    @Operation(summary = "将当前用户全部通知标记为已读")
    @PostMapping("/read-all")
    public Result<Void> readAll() {
        noticeService.readAll();
        return Result.success();
    }

    @Operation(summary = "查询当前用户通知未读总数")
    @GetMapping("/unread/count")
    public Result<NoticeUnreadCountVO> getUnreadCount() {
        return Result.success(noticeService.getUnreadCount());
    }
}
