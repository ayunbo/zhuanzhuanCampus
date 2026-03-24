package com.zhuanzhuan.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户浏览历史按日归档
 */
@Data
public class UserBrowseHistoryArchiveVO {

    private String dateKey;
    private String dateLabel;
    private List<UserBrowseHistoryItemVO> items;
}
