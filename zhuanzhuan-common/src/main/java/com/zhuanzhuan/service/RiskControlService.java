package com.zhuanzhuan.service;

import java.time.Duration;

/**
 * 风控缓存服务。
 * 由 server 模块提供 Redis 实现，业务模块只依赖该抽象。
 */
public interface RiskControlService {

    int LOGIN_FAIL_LIMIT = 5;
    Duration LOGIN_LOCK_TTL = Duration.ofMinutes(15);

    int LOGIN_IP_LIMIT = 20;
    Duration LOGIN_IP_WINDOW = Duration.ofMinutes(1);

    int REPORT_SUBMIT_LIMIT = 3;
    Duration REPORT_SUBMIT_WINDOW = Duration.ofMinutes(1);

    int SELLER_AUTH_SUBMIT_LIMIT = 1;
    Duration SELLER_AUTH_SUBMIT_WINDOW = Duration.ofMinutes(1);

    int ADMIN_AUDIT_LIMIT = 5;
    Duration ADMIN_AUDIT_WINDOW = Duration.ofSeconds(1);

    Duration DEDUP_TTL = Duration.ofSeconds(30);
    Duration AUTH_STATUS_TTL = Duration.ofMinutes(5);

    /**
     * 判断指定账号是否处于临时锁定状态。
     */
    boolean isLoginLocked(String identity, String account);

    /**
     * 记录一次登录失败，并在达到阈值时写入临时锁定标记。
     *
     * @return 当前失败次数
     */
    int recordLoginFailure(String identity, String account);

    /**
     * 清理登录失败计数和锁定标记。
     */
    void clearLoginFailures(String identity, String account);

    /**
     * 固定窗口限流。
     *
     * @return true 表示允许继续处理
     */
    boolean allowRate(String key, int limit, Duration window);

    /**
     * 获取短期防重复提交锁。
     *
     * @return true 表示获取成功
     */
    boolean acquireDedupLock(String key, Duration ttl);

    /**
     * 释放短期防重复提交锁。
     */
    void releaseDedupLock(String key);

    /**
     * 获取缓存中的账号状态。
     */
    Integer getAuthStatus(String identity, Long id);

    /**
     * 缓存账号状态。
     */
    void cacheAuthStatus(String identity, Long id, Integer status);

    /**
     * 清理账号状态缓存。
     */
    void evictAuthStatus(String identity, Long id);
}
