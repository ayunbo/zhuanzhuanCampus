package com.zhuanzhuan.dto.notify;

import lombok.Data;

@Data
public class NoticeReadDTO {

    /**
     * 要标记已读的通知 id。
     */
    private Long noticeId;
}
