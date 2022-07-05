package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

public class RedstoneModeSettings {
    private static final int IGNORE = 0;
    private static final int HIGH = 1;
    private static final int LOW = 2;

    private RedstoneModeSettings() {
    }

    public static RedstoneMode getRedstoneMode(final int redstoneMode) {
        return switch (redstoneMode) {
            case IGNORE -> RedstoneMode.IGNORE;
            case HIGH -> RedstoneMode.HIGH;
            case LOW -> RedstoneMode.LOW;
            default -> RedstoneMode.IGNORE;
        };
    }

    public static int getRedstoneMode(final RedstoneMode redstoneMode) {
        return switch (redstoneMode) {
            case IGNORE -> IGNORE;
            case HIGH -> HIGH;
            case LOW -> LOW;
        };
    }

    public static TwoWaySyncProperty<RedstoneMode> createClientSyncProperty(final int index) {
        return TwoWaySyncProperty.forClient(
            index,
            RedstoneModeSettings::getRedstoneMode,
            RedstoneModeSettings::getRedstoneMode,
            RedstoneMode.IGNORE,
            redstoneMode -> {
            }
        );
    }
}
