package com.zhuanzhuan.dto.notify;

import lombok.Data;

@Data
public class NoticeMockPublishDTO {

    private Long receiverUserId;
    private String scene;
    private Long bizId;
    private String title;
    private String content;
}
