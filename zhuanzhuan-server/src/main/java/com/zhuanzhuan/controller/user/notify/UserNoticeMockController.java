package com.zhuanzhuan.controller.user.notify;

import com.zhuanzhuan.dto.notify.NoticeMockPublishDTO;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.notify.NoticePublishService;
import com.zhuanzhuan.vo.notify.NoticeMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户通知调试接口")
@RestController
@RequestMapping("/user/notice/mock")
public class UserNoticeMockController {

    private static final String SCENE_SELLER_AUTH_RESULT = "seller_auth_result";
    private static final String SCENE_GOODS_AUDIT_RESULT = "goods_audit_result";
    private static final String SCENE_ORDER_STATUS_CHANGE = "order_status_change";
    private static final String SCENE_REPORT_RESULT = "report_result";
    private static final String SCENE_CHAT_MESSAGE_NOTICE = "chat_message_notice";

    @Autowired
    private NoticePublishService noticePublishService;

    @Operation(summary = "Publish a mock notice for development testing")
    @PostMapping("/publish")
    public Result<NoticeMessageVO> publishMock(@RequestBody NoticeMockPublishDTO noticeMockPublishDTO) {
        return Result.success(doPublish(noticeMockPublishDTO));
    }

    private NoticeMessageVO doPublish(NoticeMockPublishDTO noticeMockPublishDTO) {
        if (noticeMockPublishDTO == null || noticeMockPublishDTO.getReceiverUserId() == null) {
            throw new BaseException("receiverUserId 不能为空");
        }
        if (!StringUtils.hasText(noticeMockPublishDTO.getScene())) {
            throw new BaseException("scene 不能为空");
        }
        if (noticeMockPublishDTO.getBizId() == null) {
            throw new BaseException("bizId 不能为空");
        }

        String scene = normalizeScene(noticeMockPublishDTO.getScene());
        Long receiverUserId = noticeMockPublishDTO.getReceiverUserId();
        Long bizId = noticeMockPublishDTO.getBizId();
        String title = trimToNull(noticeMockPublishDTO.getTitle());
        String content = trimToNull(noticeMockPublishDTO.getContent());

        switch (scene) {
            case SCENE_SELLER_AUTH_RESULT:
                return noticePublishService.publishSellerAuthResult(
                        receiverUserId,
                        bizId,
                        defaultIfBlank(title, "卖家认证审核结果"),
                        defaultIfBlank(content, "你的卖家认证审核状态已更新, 点击查看审核进度.")
                );
            case SCENE_GOODS_AUDIT_RESULT:
                return noticePublishService.publishGoodsAuditResult(
                        receiverUserId,
                        bizId,
                        defaultIfBlank(title, "商品审核结果"),
                        defaultIfBlank(content, "你的商品审核状态已更新, 点击查看商品审核进度.")
                );
            case SCENE_ORDER_STATUS_CHANGE:
                return noticePublishService.publishOrderStatusChange(
                        receiverUserId,
                        bizId,
                        defaultIfBlank(title, "Order Status Updated"),
                        defaultIfBlank(content, "你的订单状态已发生变化, 点击查看订单进度.")
                );
            case SCENE_REPORT_RESULT:
                return noticePublishService.publishReportResult(
                        receiverUserId,
                        bizId,
                        defaultIfBlank(title, "举报处理结果"),
                        defaultIfBlank(content, "你的举报处理状态已更新, 点击查看处理结果.")
                );
            case SCENE_CHAT_MESSAGE_NOTICE:
                return noticePublishService.publishChatMessageNotice(
                        receiverUserId,
                        bizId,
                        defaultIfBlank(title, "新的聊天消息"),
                        defaultIfBlank(content, "你收到一条新的聊天消息, 点击查看会话.")
                );
            default:
                throw new BaseException("不支持的 scene: " + noticeMockPublishDTO.getScene());
        }
    }

    private String normalizeScene(String scene) {
        return scene == null ? null : scene.trim().toLowerCase();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
