package com.refinedmods.refinedstorage2.api.network.node;

public enum RedstoneMode {
    IGNORE,
    HIGH,
    LOW;

    public boolean isActive(boolean powered) {
        switch (this) {
            case IGNORE:
                return true;
            case HIGH:
                return powered;
            case LOW:
                return !powered;
            default:
                return false;
        }
    }

    public RedstoneMode toggle() {
        switch (this) {
            case IGNORE:
                return HIGH;
            case HIGH:
                return LOW;
            case LOW:
                return IGNORE;
            default:
                return IGNORE;
        }
    }
}
