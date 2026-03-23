package com.zhuanzhuan.vo.notify;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeMessageVO {

    private Long id;
    private Integer type;
    private String title;
    private String content;
    private Integer bizType;
    private Long bizId;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
