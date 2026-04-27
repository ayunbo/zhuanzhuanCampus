package com.zhuanzhuan.dto.notify;

import lombok.Data;

@Data
public class NoticeMessageQueryDTO {

    /**
     * 页码，默认第 1 页。
     */
    private Integer pageNo = 1;

    /**
     * 每页条数，默认 20。
     */
    private Integer pageSize = 20;

    /**
     * 读取状态过滤：0 未读，1 已读；为空时查全部。
     */
    private Integer readStatus;
}
