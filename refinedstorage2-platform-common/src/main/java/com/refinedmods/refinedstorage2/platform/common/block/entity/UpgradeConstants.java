package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.google.common.util.concurrent.RateLimiter;

public final class UpgradeConstants {
    public static final int UPGRADE_SLOTS = 4;

    private UpgradeConstants() {
    }

    public static RateLimiter getRateLimiter(final long amountOfSpeedUpgrades) {
        return RateLimiter.create(amountOfSpeedUpgrades + 1);
    }
}
