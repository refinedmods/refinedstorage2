package com.refinedmods.refinedstorage2.platform.common.storage.externalstorage;

import com.google.common.util.concurrent.RateLimiter;

class ExternalStorageWorkRate {
    private static final double[] RATE_LIMITERS = new double[] {
        0.5D, // slowest, every 2 sec
        0.75D, // faster
        1D, // medium, every 1 sec
        2D, // faster, every 0.5 sec
        3D // fastest
    };

    private int idx = 2; // medium
    private final RateLimiter rateLimiter = RateLimiter.create(RATE_LIMITERS[idx]);

    public boolean canDoWork() {
        return rateLimiter.tryAcquire();
    }

    public void faster() {
        if (idx + 1 >= RATE_LIMITERS.length) {
            return;
        }
        idx++;
        updateRate();
    }

    public void slower() {
        if (idx - 1 < 0) {
            return;
        }
        idx--;
        updateRate();
    }

    private void updateRate() {
        rateLimiter.setRate(RATE_LIMITERS[idx]);
    }
}
