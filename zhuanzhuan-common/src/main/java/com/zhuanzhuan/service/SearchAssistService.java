package com.zhuanzhuan.service;

import com.zhuanzhuan.dto.SearchHistoryRecordDTO;

import java.util.List;

/**
 * 搜索辅助服务。
 * 用于热搜统计和联想词缓存，具体实现由 server 模块提供。
 */
public interface SearchAssistService {

    /**
     * 记录一次搜索词统计。
     */
    void record(SearchHistoryRecordDTO dto);

    /**
     * 查询联想词。
     */
    List<String> suggest(String keyword, Integer limit);

    /**
     * 查询热门搜索词。
     */
    List<String> hot(Integer limit);
}
