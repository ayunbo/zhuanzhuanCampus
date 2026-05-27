package com.zhuanzhuan.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhuanzhuan.dto.SearchHistoryRecordDTO;
import com.zhuanzhuan.service.SearchAssistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Slf4j
public class RedisSearchAssistServiceImpl implements SearchAssistService {

    private static final String HOT_KEY = "search:hot:keywords";
    private static final String POOL_KEY = "search:keywords:pool";
    private static final String SUGGEST_CACHE_PREFIX = "search:suggest:";
    private static final Duration HOT_TTL = Duration.ofDays(30);
    private static final Duration SUGGEST_TTL = Duration.ofMinutes(5);
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;
    private static final int HOT_FETCH_LIMIT = 200;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void record(SearchHistoryRecordDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getKeyword())) {
            return;
        }

        try {
            String keyword = normalize(dto.getKeyword());
            Double score = stringRedisTemplate.opsForZSet().incrementScore(HOT_KEY, keyword, 1d);
            if (score != null && score == 1d) {
                stringRedisTemplate.expire(HOT_KEY, HOT_TTL);
            }
            stringRedisTemplate.opsForSet().add(POOL_KEY, keyword);
            stringRedisTemplate.expire(POOL_KEY, HOT_TTL);
        } catch (Exception ex) {
            log.warn("Redis 热搜记录失败, keyword={}", dto.getKeyword(), ex);
        }
    }

    @Override
    public List<String> suggest(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        int actualLimit = normalizeLimit(limit);
        String normalized = normalize(keyword);
        String cacheKey = SUGGEST_CACHE_PREFIX + normalized + ":" + actualLimit;

        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cached)) {
                List<String> cacheList = JSON.parseArray(cached, String.class);
                if (cacheList != null) {
                    return cacheList;
                }
            }

            Set<String> candidates = new LinkedHashSet<>();
            Set<String> hotKeywords = stringRedisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, HOT_FETCH_LIMIT - 1);
            if (hotKeywords != null) {
                for (String item : hotKeywords) {
                    if (StringUtils.hasText(item) && normalize(item).startsWith(normalized)) {
                        candidates.add(item);
                    }
                }
            }

            if (candidates.size() < actualLimit) {
                Set<String> pool = stringRedisTemplate.opsForSet().members(POOL_KEY);
                if (pool != null) {
                    for (String item : pool) {
                        if (StringUtils.hasText(item) && normalize(item).startsWith(normalized)) {
                            candidates.add(item);
                            if (candidates.size() >= actualLimit) {
                                break;
                            }
                        }
                    }
                }
            }

            List<String> result = new ArrayList<>(candidates);
            if (result.size() > actualLimit) {
                result = result.subList(0, actualLimit);
            }
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), SUGGEST_TTL);
            return result;
        } catch (Exception ex) {
            log.warn("Redis 联想词查询失败, keyword={}", keyword, ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> hot(Integer limit) {
        int actualLimit = normalizeLimit(limit);
        try {
            Set<String> values = stringRedisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, actualLimit - 1);
            if (values == null || values.isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList<>(values);
        } catch (Exception ex) {
            log.warn("Redis 热搜查询失败", ex);
            return Collections.emptyList();
        }
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
