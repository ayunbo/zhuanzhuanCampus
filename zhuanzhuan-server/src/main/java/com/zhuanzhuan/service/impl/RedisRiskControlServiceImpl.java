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
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(loginLockKey(identity, account)));
        } catch (Exception ex) {
            log.warn("Redis 不可用，跳过登录锁定检查, identity={}, account={}", identity, account, ex);
            return false;
        }
    }

    @Override
    public int recordLoginFailure(String identity, String account) {
        try {
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
        } catch (Exception ex) {
            log.warn("Redis 不可用，跳过登录失败计数, identity={}, account={}", identity, account, ex);
            return 0;
        }
    }

    @Override
    public void clearLoginFailures(String identity, String account) {
        try {
            stringRedisTemplate.delete(loginFailKey(identity, account));
            stringRedisTemplate.delete(loginLockKey(identity, account));
        } catch (Exception ex) {
            log.warn("Redis 不可用，跳过登录失败记录清理, identity={}, account={}", identity, account, ex);
        }
    }

    @Override
    public boolean allowRate(String key, int limit, Duration window) {
        if (!StringUtils.hasText(key) || limit <= 0) {
            return true;
        }
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, window);
            }
            return count == null || count <= limit;
        } catch (Exception ex) {
            log.warn("Redis 不可用，跳过限流检查, key={}", key, ex);
            return true;
        }
    }

    @Override
    public boolean acquireDedupLock(String key, Duration ttl) {
        if (!StringUtils.hasText(key)) {
            return true;
        }
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl));
        } catch (Exception ex) {
            log.warn("Redis 不可用，跳过去重锁获取, key={}", key, ex);
            return true;
        }
    }

    @Override
    public void releaseDedupLock(String key) {
        if (StringUtils.hasText(key)) {
            try {
                stringRedisTemplate.delete(key);
            } catch (Exception ex) {
                log.warn("Redis 不可用，跳过去重锁释放, key={}", key, ex);
            }
        }
    }

    @Override
    public Integer getAuthStatus(String identity, Long id) {
        try {
            String value = stringRedisTemplate.opsForValue().get(authStatusKey(identity, id));
            if (!StringUtils.hasText(value)) {
                return null;
            }
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            log.warn("账号状态缓存值非法, key={}", authStatusKey(identity, id), ex);
            return null;
        } catch (Exception ex) {
            log.warn("Redis 不可用，跳过账号状态读取, identity={}, id={}", identity, id, ex);
            return null;
        }
    }

    @Override
    public void cacheAuthStatus(String identity, Long id, Integer status) {
        if (id == null || status == null) {
            return;
        }
        try {
            Duration ttl = AUTH_STATUS_TTL.plusSeconds(ThreadLocalRandom.current().nextInt(30));
            stringRedisTemplate.opsForValue().set(authStatusKey(identity, id), status.toString(), ttl);
        } catch (Exception ex) {
            log.warn("Redis 不可用，跳过账号状态缓存, identity={}, id={}", identity, id, ex);
        }
    }

    @Override
    public void evictAuthStatus(String identity, Long id) {
        if (id != null) {
            try {
                stringRedisTemplate.delete(authStatusKey(identity, id));
            } catch (Exception ex) {
                log.warn("Redis 不可用，跳过账号状态清理, identity={}, id={}", identity, id, ex);
            }
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
