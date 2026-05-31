package com.zhuanzhuan.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeVO {

    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sort;
    private Integer status;
    private Long goodsCount;
    private List<CategoryTreeVO> children = new ArrayList<>();
}

