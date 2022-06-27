package com.refinedmods.refinedstorage2.platform.common.util;

public enum RedstoneMode {
    IGNORE,
    HIGH,
    LOW;

    public boolean isActive(final boolean powered) {
        return switch (this) {
            case IGNORE -> true;
            case HIGH -> powered;
            case LOW -> !powered;
        };
    }

    public RedstoneMode toggle() {
        return switch (this) {
            case IGNORE -> HIGH;
            case HIGH -> LOW;
            case LOW -> IGNORE;
        };
    }
}
