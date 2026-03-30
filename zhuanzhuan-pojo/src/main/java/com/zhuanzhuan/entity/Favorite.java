package com.zhuanzhuan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    private Long id;
    private Long userId;
    private Long goodsId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
