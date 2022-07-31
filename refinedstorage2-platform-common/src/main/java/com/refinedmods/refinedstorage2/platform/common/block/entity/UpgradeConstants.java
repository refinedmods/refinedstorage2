package com.refinedmods.refinedstorage2.platform.common.block.entity;

public final class UpgradeConstants {
    public static final int UPGRADE_SLOTS = 4;
    public static final long DEFAULT_COOL_DOWN_TIMER = 8;

    private UpgradeConstants() {
    }

    public static long calculateCoolDownTime(final long amountOfSpeedUpgrades) {
        return DEFAULT_COOL_DOWN_TIMER
            - (long) ((double) amountOfSpeedUpgrades / (double) UPGRADE_SLOTS * (double) DEFAULT_COOL_DOWN_TIMER);
    }
}
