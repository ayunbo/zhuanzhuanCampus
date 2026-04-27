package com.zhuanzhuan.vo.notify;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeSessionSummaryVO {

    /**
     * 前端虚拟系统通知会话的固定 key。
     */
    private String sessionKey;

    /**
     * 前端展示名称，例如“通知消息”。
     */
    private String sessionName;

    private Long lastNoticeId;
    private String lastTitle;
    private String lastMsg;
    private LocalDateTime lastTime;
    private Integer unreadCount;
}
