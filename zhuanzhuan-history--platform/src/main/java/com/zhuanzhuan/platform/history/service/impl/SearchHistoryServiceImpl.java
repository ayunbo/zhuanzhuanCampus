package com.zhuanzhuan.platform.history.service.impl;

import com.zhuanzhuan.constant.MessageConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.SearchHistoryQueryDTO;
import com.zhuanzhuan.dto.SearchHistoryRecordDTO;
import com.zhuanzhuan.entity.SearchHistory;
import com.zhuanzhuan.exception.BaseException;
import com.zhuanzhuan.platform.history.mapper.SearchHistoryMapper;
import com.zhuanzhuan.platform.history.service.SearchHistoryService;
import com.zhuanzhuan.vo.SearchHistoryArchiveVO;
import com.zhuanzhuan.vo.SearchHistoryItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Override
    public void record(SearchHistoryRecordDTO dto) {
        if (dto == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        if (!hasMeaningfulCondition(dto)) {
            return;
        }

        Long userId = requireCurrentUserId();
        String querySign = buildQuerySign(dto);

        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .querySign(querySign)
                .keyword(normalizeText(dto.getKeyword()))
                .categoryId(dto.getCategoryId())
                .sellerId(dto.getSellerId())
                .status(dto.getStatus())
                .quality(dto.getQuality())
                .location(normalizeText(dto.getLocation()))
                .minPrice(dto.getMinPrice())
                .maxPrice(dto.getMaxPrice())
                .sortBy(normalizeText(dto.getSortBy()))
                .searchCount(1)
                .lastSearchTime(LocalDateTime.now())
                .build();
        searchHistoryMapper.upsert(history);
    }

    @Override
    public List<SearchHistoryArchiveVO> archive(SearchHistoryQueryDTO dto) {
        Long userId = requireCurrentUserId();
        int limit = normalizeLimit(dto);
        List<SearchHistoryItemVO> items = searchHistoryMapper.listByUserId(userId, limit);
        return groupByDate(items);
    }

    @Override
    public void remove(Long historyId) {
        if (historyId == null) {
            throw new BaseException(MessageConstant.REQUEST_PARAM_NULL);
        }

        Long userId = requireCurrentUserId();
        searchHistoryMapper.deleteByUserIdAndId(userId, historyId);
    }

    @Override
    public void clear() {
        Long userId = requireCurrentUserId();
        searchHistoryMapper.deleteByUserId(userId);
    }

    private boolean hasMeaningfulCondition(SearchHistoryRecordDTO dto) {
        return StringUtils.hasText(dto.getKeyword())
                || dto.getCategoryId() != null
                || dto.getSellerId() != null
                || dto.getStatus() != null
                || dto.getQuality() != null
                || StringUtils.hasText(dto.getLocation())
                || dto.getMinPrice() != null
                || dto.getMaxPrice() != null
                || StringUtils.hasText(dto.getSortBy());
    }

    private String buildQuerySign(SearchHistoryRecordDTO dto) {
        String raw = Arrays.asList(
                normalizeText(dto.getKeyword()),
                dto.getCategoryId(),
                dto.getSellerId(),
                dto.getStatus(),
                dto.getQuality(),
                normalizeText(dto.getLocation()),
                dto.getMinPrice(),
                dto.getMaxPrice(),
                normalizeText(dto.getSortBy())
        ).toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            return raw;
        }
    }

    private Long requireCurrentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        return userId;
    }

    private int normalizeLimit(SearchHistoryQueryDTO dto) {
        if (dto == null || dto.getLimit() == null || dto.getLimit() <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(dto.getLimit(), MAX_LIMIT);
    }

    private List<SearchHistoryArchiveVO> groupByDate(List<SearchHistoryItemVO> items) {
        Map<LocalDate, List<SearchHistoryItemVO>> grouped = new LinkedHashMap<>();
        for (SearchHistoryItemVO item : items) {
            if (item == null || item.getLastSearchTime() == null) {
                continue;
            }
            LocalDate date = item.getLastSearchTime().toLocalDate();
            grouped.computeIfAbsent(date, key -> new ArrayList<>()).add(item);
        }

        LocalDate today = LocalDate.now();
        List<SearchHistoryArchiveVO> archives = new ArrayList<>(grouped.size());
        for (Map.Entry<LocalDate, List<SearchHistoryItemVO>> entry : grouped.entrySet()) {
            SearchHistoryArchiveVO archive = new SearchHistoryArchiveVO();
            archive.setDateKey(entry.getKey().toString());
            archive.setDateLabel(resolveDateLabel(today, entry.getKey()));
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

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
