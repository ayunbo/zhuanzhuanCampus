package com.zhuanzhuan.service.notify;

import com.zhuanzhuan.dto.notify.NoticePublishCommand;
import com.zhuanzhuan.entity.notify.Notice;
import com.zhuanzhuan.vo.notify.NoticeMessageVO;

public interface NoticePublishService {

    NoticeMessageVO publish(NoticePublishCommand command);

    void readChatMessageNotices(Long userId, Long sessionId);

    default NoticeMessageVO publishSellerAuthResult(Long receiverUserId,
                                                    Long sellerAuthId,
                                                    String title,
                                                    String content) {
        return publish(NoticePublishCommand.builder()
                .receiverUserId(receiverUserId)
                .type(Notice.TYPE_AUDIT)
                .bizType(Notice.BIZ_TYPE_SELLER_AUTH)
                .bizId(sellerAuthId)
                .title(title)
                .content(content)
                .build());
    }

    default NoticeMessageVO publishGoodsAuditResult(Long receiverUserId,
                                                    Long goodsId,
                                                    String title,
                                                    String content) {
        return publish(NoticePublishCommand.builder()
                .receiverUserId(receiverUserId)
                .type(Notice.TYPE_AUDIT)
                .bizType(Notice.BIZ_TYPE_GOODS)
                .bizId(goodsId)
                .title(title)
                .content(content)
                .build());
    }

    default NoticeMessageVO publishOrderStatusChange(Long receiverUserId,
                                                     Long orderId,
                                                     String title,
                                                     String content) {
        return publish(NoticePublishCommand.builder()
                .receiverUserId(receiverUserId)
                .type(Notice.TYPE_ORDER)
                .bizType(Notice.BIZ_TYPE_ORDER)
                .bizId(orderId)
                .title(title)
                .content(content)
                .build());
    }

    default NoticeMessageVO publishReportResult(Long receiverUserId,
                                                Long reportId,
                                                String title,
                                                String content) {
        return publish(NoticePublishCommand.builder()
                .receiverUserId(receiverUserId)
                .type(Notice.TYPE_REPORT)
                .bizType(Notice.BIZ_TYPE_REPORT)
                .bizId(reportId)
                .title(title)
                .content(content)
                .build());
    }

    default NoticeMessageVO publishChatMessageNotice(Long receiverUserId,
                                                     Long sessionId,
                                                     String title,
                                                     String content) {
        return publish(NoticePublishCommand.builder()
                .receiverUserId(receiverUserId)
                .type(Notice.TYPE_CHAT)
                .bizType(Notice.BIZ_TYPE_CHAT)
                .bizId(sessionId)
                .title(title)
                .content(content)
                .build());
    }
}
