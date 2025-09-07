package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import java.util.concurrent.atomic.AtomicBoolean;

public class TimeoutableCancellationToken implements CancellationToken {
    private static final long TIMEOUT_MS = 5000;

    private final long createdAt = System.currentTimeMillis();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public boolean isCancelled() {
        if (System.currentTimeMillis() - createdAt >= TIMEOUT_MS) {
            cancel();
        }
        return cancelled.get();
    }

    public void cancel() {
        cancelled.set(true);
    }
}
