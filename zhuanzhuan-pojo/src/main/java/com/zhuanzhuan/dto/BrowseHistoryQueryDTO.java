package com.zhuanzhuan.dto;

import lombok.Data;

/**
 * 浏览历史查询参数
 */
@Data
public class BrowseHistoryQueryDTO {

    /**
     * 返回记录上限，默认 200
     */
    private Integer limit = 200;
}
