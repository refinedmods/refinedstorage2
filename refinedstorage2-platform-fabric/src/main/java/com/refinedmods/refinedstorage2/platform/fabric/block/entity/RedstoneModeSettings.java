package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;

public class RedstoneModeSettings {
    private static final int IGNORE = 0;
    private static final int HIGH = 1;
    private static final int LOW = 2;

    private RedstoneModeSettings() {
    }

    public static RedstoneMode getRedstoneMode(int redstoneMode) {
        return switch (redstoneMode) {
            case IGNORE -> RedstoneMode.IGNORE;
            case HIGH -> RedstoneMode.HIGH;
            case LOW -> RedstoneMode.LOW;
            default -> RedstoneMode.IGNORE;
        };
    }

    public static int getRedstoneMode(RedstoneMode redstoneMode) {
        return switch (redstoneMode) {
            case IGNORE -> IGNORE;
            case HIGH -> HIGH;
            case LOW -> LOW;
        };
    }
}
