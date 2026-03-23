package com.zhuanzhuan.entity.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    public static final Integer READ_UNREAD = 0;
    public static final Integer READ_READ = 1;

    private Long id;
    private Long userId;
    private Integer type;
    private String title;
    private String content;
    private Integer bizType;
    private Long bizId;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
