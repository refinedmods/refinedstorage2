package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.RedstoneMode;

public class RedstoneModeSettings {
    private static final int IGNORE = 0;
    private static final int HIGH = 1;
    private static final int LOW = 2;

    private RedstoneModeSettings() {
    }

    public static RedstoneMode getRedstoneMode(int redstoneMode) {
        switch (redstoneMode) {
            case IGNORE:
                return RedstoneMode.IGNORE;
            case HIGH:
                return RedstoneMode.HIGH;
            case LOW:
                return RedstoneMode.LOW;
            default:
                return RedstoneMode.IGNORE;
        }
    }

    public static int getRedstoneMode(RedstoneMode redstoneMode) {
        switch (redstoneMode) {
            case IGNORE:
                return IGNORE;
            case HIGH:
                return HIGH;
            case LOW:
                return LOW;
            default:
                return IGNORE;
        }
    }
}
