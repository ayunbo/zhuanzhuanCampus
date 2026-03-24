package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 浏览历史实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseHistory {

    private Long id;
    private Long userId;
    private Long goodsId;
    private Integer browseCount;
    private LocalDateTime lastBrowseTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
