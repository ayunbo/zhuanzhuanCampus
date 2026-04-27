package com.zhuanzhuan.service.impl.notify;

import com.zhuanzhuan.dto.notify.NoticePublishCommand;
import com.zhuanzhuan.entity.notify.Notice;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.mapper.notify.NoticeMapper;
import com.zhuanzhuan.service.notify.NoticePublishService;
import com.zhuanzhuan.utils.IdGenerator;
import com.zhuanzhuan.vo.notify.NoticeMessageVO;
import com.zhuanzhuan.vo.notify.NoticeUnreadCountVO;
import com.zhuanzhuan.websocket.notify.NoticeWebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class NoticePublishServiceImpl implements NoticePublishService {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int CONTENT_MAX_LENGTH = 500;
    private static final Long SYSTEM_OPERATOR_ID = 0L;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private NoticeViewSupport noticeViewSupport;

    @Autowired
    private NoticeWebSocketPublisher noticeWebSocketPublisher;

    @Override
    @Transactional
    public NoticeMessageVO publish(NoticePublishCommand command) {
        if (command == null) {
            throw new BaseException("通知发布参数不能为空");
        }

        Long receiverUserId = command.getReceiverUserId();
        if (receiverUserId == null) {
            throw new BaseException("receiverUserId 不能为空");
        }
        if (command.getType() == null || command.getBizType() == null || command.getBizId() == null) {
            throw new BaseException("type、bizType、bizId 不能为空");
        }

        String title = trimToNull(command.getTitle());
        String content = trimToNull(command.getContent());
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new BaseException("title 和 content 不能为空");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new BaseException("通知标题长度不能超过 100");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new BaseException("通知内容长度不能超过 500");
        }

        LocalDateTime now = LocalDateTime.now();
        Notice notice = Notice.builder()
                .id(IdGenerator.nextId())
                .userId(receiverUserId)
                .type(command.getType())
                .title(title)
                .content(content)
                .bizType(command.getBizType())
                .bizId(command.getBizId())
                .readStatus(Notice.READ_UNREAD)
                .createTime(now)
                .updateTime(now)
                .createUser(SYSTEM_OPERATOR_ID)
                .updateUser(SYSTEM_OPERATOR_ID)
                .build();

        int rows = noticeMapper.insert(notice);
        if (rows <= 0) {
            throw new BaseException("通知发布失败");
        }

        NoticeMessageVO messageVO = noticeViewSupport.buildMessageVO(notice);
        noticeWebSocketPublisher.sendToUser(receiverUserId, "notice.message", messageVO);
        noticeWebSocketPublisher.sendToUser(receiverUserId, "notice.unread", getUnreadCountByUserId(receiverUserId));
        return messageVO;
    }

    @Override
    @Transactional
    public void readChatMessageNotices(Long userId, Long sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        int rows = noticeMapper.markChatNoticesReadByBizId(userId, sessionId, LocalDateTime.now());
        if (rows > 0) {
            noticeWebSocketPublisher.sendToUser(userId, "notice.unread", getUnreadCountByUserId(userId));
        }
    }

    private NoticeUnreadCountVO getUnreadCountByUserId(Long userId) {
        Integer totalUnread = noticeMapper.countUnreadByUserId(userId);
        return new NoticeUnreadCountVO(totalUnread == null ? 0 : totalUnread);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
