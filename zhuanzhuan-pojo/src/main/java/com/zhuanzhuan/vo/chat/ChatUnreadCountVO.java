package com.zhuanzhuan.vo.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUnreadCountVO {

    /**
     * 当前登录用户聊天未读总数。
     */
    private Integer totalUnreadCount;
}
