package com.zhuanzhuan.service.impl.notify;

import com.zhuanzhuan.entity.notify.Notice;
import com.zhuanzhuan.vo.notify.NoticeMessageVO;
import org.springframework.stereotype.Component;

@Component
public class NoticeViewSupport {

    private static final String TARGET_PAGE_GOODS_AUDIT = "goodsAudit";
    private static final String TARGET_PAGE_ORDER = "order";
    private static final String TARGET_PAGE_SELLER_AUTH = "sellerAuth";
    private static final String TARGET_PAGE_CHAT = "chat";
    private static final String TARGET_PAGE_REPORT = "report";
    private static final String TARGET_PAGE_NOTICE = "notice";

    public NoticeMessageVO buildMessageVO(Notice notice) {
        NoticeMessageVO messageVO = new NoticeMessageVO();
        messageVO.setId(notice.getId());
        messageVO.setType(notice.getType());
        messageVO.setTitle(notice.getTitle());
        messageVO.setContent(notice.getContent());
        messageVO.setBizType(notice.getBizType());
        messageVO.setBizId(notice.getBizId());
        messageVO.setReadStatus(notice.getReadStatus());
        messageVO.setReadTime(notice.getReadTime());
        messageVO.setCreateTime(notice.getCreateTime());
        fillDisplayFields(messageVO);
        return messageVO;
    }

    public void fillDisplayFields(NoticeMessageVO messageVO) {
        if (messageVO == null) {
            return;
        }
        messageVO.setTargetId(messageVO.getBizId());
        Integer bizType = messageVO.getBizType();
        if (Notice.BIZ_TYPE_GOODS.equals(bizType)) {
            messageVO.setActionText("查看商品审核进度");
            messageVO.setTargetPage(TARGET_PAGE_GOODS_AUDIT);
            return;
        }
        if (Notice.BIZ_TYPE_ORDER.equals(bizType)) {
            messageVO.setActionText("查看订单进度");
            messageVO.setTargetPage(TARGET_PAGE_ORDER);
            return;
        }
        if (Notice.BIZ_TYPE_SELLER_AUTH.equals(bizType)) {
            messageVO.setActionText("查看认证审核进度");
            messageVO.setTargetPage(TARGET_PAGE_SELLER_AUTH);
            return;
        }
        if (Notice.BIZ_TYPE_CHAT.equals(bizType)) {
            messageVO.setActionText("查看聊天会话");
            messageVO.setTargetPage(TARGET_PAGE_CHAT);
            return;
        }
        if (Notice.BIZ_TYPE_REPORT.equals(bizType)) {
            messageVO.setActionText("查看举报处理结果");
            messageVO.setTargetPage(TARGET_PAGE_REPORT);
            return;
        }
        messageVO.setActionText("查看详情");
        messageVO.setTargetPage(TARGET_PAGE_NOTICE);
    }
}
