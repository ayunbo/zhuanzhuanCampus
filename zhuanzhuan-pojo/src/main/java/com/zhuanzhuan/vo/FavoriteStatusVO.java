package com.zhuanzhuan.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏状态返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStatusVO {

    private Long goodsId;
    private Boolean favorited;
}
