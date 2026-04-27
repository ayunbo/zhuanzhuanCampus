package com.zhuanzhuan.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Process-local long ID generator used by modules whose tables require caller-provided IDs.
 */
public final class IdGenerator {

    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis());

    private IdGenerator() {
    }

    public static long nextId() {
        return COUNTER.incrementAndGet();
    }
}
