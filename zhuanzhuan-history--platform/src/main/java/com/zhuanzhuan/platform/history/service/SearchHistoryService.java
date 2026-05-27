package com.zhuanzhuan.platform.history.service;

import com.zhuanzhuan.dto.SearchHistoryQueryDTO;
import com.zhuanzhuan.dto.SearchHistoryRecordDTO;
import com.zhuanzhuan.vo.SearchHistoryArchiveVO;

import java.util.List;

public interface SearchHistoryService {

    void record(SearchHistoryRecordDTO dto);

    List<SearchHistoryArchiveVO> archive(SearchHistoryQueryDTO dto);

    void remove(Long historyId);

    void clear();
}
