package com.refinedmods.refinedstorage2.fabric.util;

import java.util.HashMap;
import java.util.Map;

public class ThrottleHelper<T> {
    private final Map<T, Long> time = new HashMap<>();
    private final int timeoutMs;

    public ThrottleHelper(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void throttle(T key, Runnable action) {
        long last = time.getOrDefault(key, 0L);
        long current = System.currentTimeMillis();

        if (current - last > timeoutMs) {
            time.put(key, current);
            action.run();
        }
    }
}
