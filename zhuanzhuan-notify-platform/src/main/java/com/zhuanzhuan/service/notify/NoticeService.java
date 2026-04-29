package com.zhuanzhuan.service.notify;

import com.zhuanzhuan.dto.notify.NoticeMessageQueryDTO;
import com.zhuanzhuan.dto.notify.NoticeReadDTO;
import com.zhuanzhuan.vo.notify.NoticeMessageVO;
import com.zhuanzhuan.vo.notify.NoticeSessionSummaryVO;
import com.zhuanzhuan.vo.notify.NoticeUnreadCountVO;

import java.util.List;

public interface NoticeService {

    NoticeSessionSummaryVO getSessionSummary();

    List<NoticeMessageVO> listMessages(NoticeMessageQueryDTO noticeMessageQueryDTO);

    void readNotice(NoticeReadDTO noticeReadDTO);

    void readAll();

    NoticeUnreadCountVO getUnreadCount();
}
