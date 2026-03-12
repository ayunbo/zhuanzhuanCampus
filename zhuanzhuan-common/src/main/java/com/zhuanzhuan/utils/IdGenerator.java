package com.zhuanzhuan.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight process-local ID generator.
 * Generates monotonically increasing long IDs.
 */
public final class IdGenerator {

    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis());

    private IdGenerator() {
    }

    public static long nextId() {
        return COUNTER.incrementAndGet();
    }
}
