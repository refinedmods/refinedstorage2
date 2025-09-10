package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;

import java.util.concurrent.atomic.AtomicBoolean;

@Deprecated // use the one in network-api
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
