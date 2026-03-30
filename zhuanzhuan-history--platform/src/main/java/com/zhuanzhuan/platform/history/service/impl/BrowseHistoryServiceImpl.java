package com.zhuanzhuan.platform.history.service.impl;

import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.BrowseHistoryQueryDTO;
import com.zhuanzhuan.entity.BrowseHistory;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.history.mapper.BrowseHistoryMapper;
import com.zhuanzhuan.platform.history.service.BrowseHistoryService;
import com.zhuanzhuan.vo.UserBrowseHistoryArchiveVO;
import com.zhuanzhuan.vo.UserBrowseHistoryItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrowseHistoryServiceImpl implements BrowseHistoryService {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private BrowseHistoryMapper browseHistoryMapper;

    @Override
    public void record(Long goodsId) {
        if (goodsId == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Long userId = requireCurrentUserId();
        validateGoods(goodsId);

        BrowseHistory browseHistory = BrowseHistory.builder()
                .userId(userId)
                .goodsId(goodsId)
                .browseCount(1)
                .lastBrowseTime(LocalDateTime.now())
                .build();
        browseHistoryMapper.upsert(browseHistory);
    }

    @Override
    public List<UserBrowseHistoryArchiveVO> archive(BrowseHistoryQueryDTO dto) {
        Long userId = requireCurrentUserId();
        int limit = normalizeLimit(dto);
        List<UserBrowseHistoryItemVO> items = browseHistoryMapper.listByUserId(userId, limit);
        return groupByDate(items);
    }

    @Override
    public void remove(Long historyId) {
        if (historyId == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Long userId = requireCurrentUserId();
        browseHistoryMapper.deleteByUserIdAndId(userId, historyId);
    }

    @Override
    public void clear() {
        Long userId = requireCurrentUserId();
        browseHistoryMapper.deleteByUserId(userId);
    }

    private void validateGoods(Long goodsId) {
        Integer count = browseHistoryMapper.countGoodsById(goodsId);
        if (count == null || count <= 0) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
    }

    private Long requireCurrentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        return userId;
    }

    private int normalizeLimit(BrowseHistoryQueryDTO dto) {
        if (dto == null || dto.getLimit() == null || dto.getLimit() <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(dto.getLimit(), MAX_LIMIT);
    }

    private List<UserBrowseHistoryArchiveVO> groupByDate(List<UserBrowseHistoryItemVO> items) {
        Map<LocalDate, List<UserBrowseHistoryItemVO>> grouped = new LinkedHashMap<>();
        for (UserBrowseHistoryItemVO item : items) {
            if (item == null || item.getBrowseTime() == null) {
                continue;
            }
            LocalDate date = item.getBrowseTime().toLocalDate();
            grouped.computeIfAbsent(date, key -> new ArrayList<>()).add(item);
        }

        LocalDate today = LocalDate.now();
        List<UserBrowseHistoryArchiveVO> archives = new ArrayList<>(grouped.size());
        for (Map.Entry<LocalDate, List<UserBrowseHistoryItemVO>> entry : grouped.entrySet()) {
            LocalDate date = entry.getKey();
            UserBrowseHistoryArchiveVO archive = new UserBrowseHistoryArchiveVO();
            archive.setDateKey(date.toString());
            archive.setDateLabel(resolveDateLabel(today, date));
            archive.setItems(entry.getValue());
            archives.add(archive);
        }
        return archives;
    }

    private String resolveDateLabel(LocalDate today, LocalDate date) {
        if (date.equals(today)) {
            return "今天";
        }
        if (date.equals(today.minusDays(1))) {
            return "昨天";
        }
        return DATE_LABEL_FORMATTER.format(date);
    }
}
