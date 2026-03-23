package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public Integer getPageSize() {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }
}
