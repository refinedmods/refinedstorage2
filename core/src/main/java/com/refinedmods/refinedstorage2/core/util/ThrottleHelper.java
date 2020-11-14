package com.refinedmods.refinedstorage2.core.util;

import java.util.HashMap;
import java.util.Map;

public class ThrottleHelper<T> {
    private final Map<T, Long> time = new HashMap<>();
    private final int timeout;

    public ThrottleHelper(int timeout) {
        this.timeout = timeout;
    }

    public boolean throttle(T key, Runnable action, long currentTime) {
        long last = time.getOrDefault(key, -1L);

        if (last == -1L || currentTime - last >= timeout) {
            time.put(key, currentTime);
            action.run();
            return true;
        }

        return false;
    }
}
