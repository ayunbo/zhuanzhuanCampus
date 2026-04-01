package com.zhuanzhuan.service.impl.notify;

import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.notify.NoticeMessageQueryDTO;
import com.zhuanzhuan.dto.notify.NoticeReadDTO;
import com.zhuanzhuan.entity.notify.Notice;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.exception.UserNotLoginException;
import com.zhuanzhuan.mapper.notify.NoticeMapper;
import com.zhuanzhuan.service.notify.NoticeService;
import com.zhuanzhuan.vo.notify.NoticeMessageVO;
import com.zhuanzhuan.vo.notify.NoticeSessionSummaryVO;
import com.zhuanzhuan.vo.notify.NoticeUnreadCountVO;
import com.zhuanzhuan.websocket.notify.NoticeWebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String SESSION_KEY = "system-notice";
    private static final String SESSION_NAME = "通知消息";

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private NoticeViewSupport noticeViewSupport;

    @Autowired
    private NoticeWebSocketPublisher noticeWebSocketPublisher;

    @Override
    public NoticeSessionSummaryVO getSessionSummary() {
        Long currentUserId = getCurrentUserId();
        Notice latestNotice = noticeMapper.getLatestByUserId(currentUserId);
        NoticeSessionSummaryVO summaryVO = new NoticeSessionSummaryVO();
        summaryVO.setSessionKey(SESSION_KEY);
        summaryVO.setSessionName(SESSION_NAME);
        summaryVO.setUnreadCount(getUnreadCountByUserId(currentUserId).getTotalUnreadCount());
        if (latestNotice != null) {
            summaryVO.setLastNoticeId(latestNotice.getId());
            summaryVO.setLastTitle(latestNotice.getTitle());
            summaryVO.setLastMsg(buildLastMsg(latestNotice));
            summaryVO.setLastTime(latestNotice.getCreateTime());
        }
        return summaryVO;
    }

    @Override
    public List<NoticeMessageVO> listMessages(NoticeMessageQueryDTO noticeMessageQueryDTO) {
        NoticeMessageQueryDTO queryDTO = noticeMessageQueryDTO == null ? new NoticeMessageQueryDTO() : noticeMessageQueryDTO;
        Integer readStatus = queryDTO.getReadStatus();
        if (readStatus != null && !Notice.READ_UNREAD.equals(readStatus) && !Notice.READ_READ.equals(readStatus)) {
            throw new BaseException("readStatus 只能是 0、1 或为空");
        }

        int pageNo = normalizePageNo(queryDTO.getPageNo());
        int pageSize = normalizePageSize(queryDTO.getPageSize());
        int offset = (pageNo - 1) * pageSize;
        List<NoticeMessageVO> noticeList = noticeMapper.listByUserId(getCurrentUserId(), readStatus, offset, pageSize);
        if (noticeList != null) {
            noticeList.forEach(noticeViewSupport::fillDisplayFields);
        }
        return noticeList == null ? Collections.emptyList() : noticeList;
    }

    @Override
    @Transactional
    public void readNotice(NoticeReadDTO noticeReadDTO) {
        if (noticeReadDTO == null || noticeReadDTO.getNoticeId() == null) {
            throw new BaseException("noticeId 不能为空");
        }

        Long currentUserId = getCurrentUserId();
        Notice notice = noticeMapper.getByIdAndUserId(noticeReadDTO.getNoticeId(), currentUserId);
        if (notice == null) {
            throw new BaseException("通知不存在");
        }
        if (Notice.READ_READ.equals(notice.getReadStatus())) {
            return;
        }

        noticeMapper.markRead(notice.getId(), currentUserId, LocalDateTime.now());
        noticeWebSocketPublisher.sendToUser(currentUserId, "notice.unread", getUnreadCountByUserId(currentUserId));
    }

    @Override
    @Transactional
    public void readAll() {
        Long currentUserId = getCurrentUserId();
        noticeMapper.markAllRead(currentUserId, LocalDateTime.now());
        noticeWebSocketPublisher.sendToUser(currentUserId, "notice.unread", getUnreadCountByUserId(currentUserId));
    }

    @Override
    public NoticeUnreadCountVO getUnreadCount() {
        return getUnreadCountByUserId(getCurrentUserId());
    }

    private NoticeUnreadCountVO getUnreadCountByUserId(Long userId) {
        Integer totalUnread = noticeMapper.countUnreadByUserId(userId);
        return new NoticeUnreadCountVO(totalUnread == null ? 0 : totalUnread);
    }

    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String buildLastMsg(Notice notice) {
        String title = StringUtils.hasText(notice.getTitle()) ? notice.getTitle().trim() : "";
        String content = StringUtils.hasText(notice.getContent()) ? notice.getContent().trim() : "";
        if (StringUtils.hasText(title) && StringUtils.hasText(content)) {
            return title + " - " + content;
        }
        return StringUtils.hasText(content) ? content : title;
    }

    private Long getCurrentUserId() {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UserNotLoginException("用户未登录");
        }
        return currentUserId;
    }
}
