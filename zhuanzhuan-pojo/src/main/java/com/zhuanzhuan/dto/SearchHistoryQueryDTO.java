package com.zhuanzhuan.dto;

import lombok.Data;

@Data
public class SearchHistoryQueryDTO {

    private Integer limit = 200;

    public Integer getLimit() {
        return limit == null || limit < 1 ? 200 : limit;
    }
}
