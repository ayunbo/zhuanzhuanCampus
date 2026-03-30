package com.zhuanzhuan.platform.history.service;

import com.zhuanzhuan.dto.BrowseHistoryQueryDTO;
import com.zhuanzhuan.vo.UserBrowseHistoryArchiveVO;

import java.util.List;

public interface BrowseHistoryService {

    void record(Long goodsId);

    List<UserBrowseHistoryArchiveVO> archive(BrowseHistoryQueryDTO dto);

    void remove(Long historyId);

    void clear();
}
