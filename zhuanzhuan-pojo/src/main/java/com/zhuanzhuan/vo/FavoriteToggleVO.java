package com.zhuanzhuan.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏/取消收藏结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteToggleVO {

    private Long goodsId;
    private Boolean favorited;
    private Integer favoriteCount;
}
