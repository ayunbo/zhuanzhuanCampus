package com.zhuanzhuan.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;
    /**
     * 0: 全部 1: 好评(5星) 2: 中评(3-4星) 3: 差评(1-2星)
     */
    private Integer scoreType = 0;

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public Integer getPageSize() {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    public Integer getScoreType() {
        if (scoreType == null || scoreType < 0 || scoreType > 3) {
            return 0;
        }
        return scoreType;
    }
}
