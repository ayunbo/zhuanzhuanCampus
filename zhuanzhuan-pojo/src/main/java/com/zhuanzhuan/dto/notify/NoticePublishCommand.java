package com.zhuanzhuan.dto.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticePublishCommand {

    private Long receiverUserId;
    private Integer type;
    private Integer bizType;
    private Long bizId;
    private String title;
    private String content;
}
