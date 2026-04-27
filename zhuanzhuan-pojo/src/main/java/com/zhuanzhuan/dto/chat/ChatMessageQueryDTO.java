package com.zhuanzhuan.dto.chat;

import lombok.Data;

@Data
public class ChatMessageQueryDTO {

    /**
     * 会话 id。
     */
    private Long sessionId;

    /**
     * 页码，默认第 1 页。
     */
    private Integer pageNo = 1;

    /**
     * 每页条数，默认 20。
     */
    private Integer pageSize = 20;
}
