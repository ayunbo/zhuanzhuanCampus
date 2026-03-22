package com.zhuanzhuan.vo;

import lombok.Data;

/**
 * 商品图片返回对象。
 */
@Data
public class GoodsImageVO {

    /**
     * 图片地址。
     */
    private String url;

    /**
     * 图片排序值。
     */
    private Integer sort;

    /**
     * 是否为封面图。
     */
    private Integer isCover;
}
