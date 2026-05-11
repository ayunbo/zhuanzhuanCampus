package com.zhuanzhuan.service.impl;

import com.zhuanzhuan.service.RiskControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Redis 风控缓存实现。
 */
@Service
@Slf4j
public class RedisRiskControlServiceImpl implements RiskControlService {

    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String LOGIN_LOCK_PREFIX = "login:lock:";
    private static final String AUTH_STATUS_PREFIX = "auth:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean isLoginLocked(String identity, String account) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(loginLockKey(identity, account)));
    }

    @Override
    public int recordLoginFailure(String identity, String account) {
        String failKey = loginFailKey(identity, account);
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(failKey, LOGIN_LOCK_TTL);
        }
        int failCount = count == null ? 1 : count.intValue();
        if (failCount >= LOGIN_FAIL_LIMIT) {
            stringRedisTemplate.opsForValue().set(loginLockKey(identity, account), "1", LOGIN_LOCK_TTL);
        }
        return failCount;
    }

    @Override
    public void clearLoginFailures(String identity, String account) {
        stringRedisTemplate.delete(loginFailKey(identity, account));
        stringRedisTemplate.delete(loginLockKey(identity, account));
    }

    @Override
    public boolean allowRate(String key, int limit, Duration window) {
        if (!StringUtils.hasText(key) || limit <= 0) {
            return true;
        }
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, window);
        }
        return count == null || count <= limit;
    }

    @Override
    public boolean acquireDedupLock(String key, Duration ttl) {
        if (!StringUtils.hasText(key)) {
            return true;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl));
    }

    @Override
    public void releaseDedupLock(String key) {
        if (StringUtils.hasText(key)) {
            stringRedisTemplate.delete(key);
        }
    }

    @Override
    public Integer getAuthStatus(String identity, Long id) {
        String value = stringRedisTemplate.opsForValue().get(authStatusKey(identity, id));
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            log.warn("账号状态缓存值非法, key={}, value={}", authStatusKey(identity, id), value);
            return null;
        }
    }

    @Override
    public void cacheAuthStatus(String identity, Long id, Integer status) {
        if (id == null || status == null) {
            return;
        }
        Duration ttl = AUTH_STATUS_TTL.plusSeconds(ThreadLocalRandom.current().nextInt(30));
        stringRedisTemplate.opsForValue().set(authStatusKey(identity, id), status.toString(), ttl);
    }

    @Override
    public void evictAuthStatus(String identity, Long id) {
        if (id != null) {
            stringRedisTemplate.delete(authStatusKey(identity, id));
        }
    }

    private String loginFailKey(String identity, String account) {
        return LOGIN_FAIL_PREFIX + normalize(identity) + ":" + normalize(account);
    }

    private String loginLockKey(String identity, String account) {
        return LOGIN_LOCK_PREFIX + normalize(identity) + ":" + normalize(account);
    }

    private String authStatusKey(String identity, Long id) {
        return AUTH_STATUS_PREFIX + normalize(identity) + ":status:" + id;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : "unknown";
    }
}
