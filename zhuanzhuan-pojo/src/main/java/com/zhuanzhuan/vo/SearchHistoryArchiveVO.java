package com.zhuanzhuan.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchHistoryArchiveVO {

    private String dateKey;
    private String dateLabel;
    private List<SearchHistoryItemVO> items;
}
