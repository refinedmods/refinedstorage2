package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.platform.abstractions.GridConfigSynchronizationType;

public enum GridSynchronizationType {
    OFF,
    ON,
    TWO_WAY;

    public static GridSynchronizationType ofConfig(GridConfigSynchronizationType type) {
        return switch (type) {
            case OFF -> OFF;
            case ON -> ON;
            case TWO_WAY -> TWO_WAY;
        };
    }

    public GridSynchronizationType toggle() {
        return switch (this) {
            case OFF -> ON;
            case ON -> TWO_WAY;
            case TWO_WAY -> OFF;
        };
    }

    public GridConfigSynchronizationType toConfig() {
        return switch (this) {
            case OFF -> GridConfigSynchronizationType.OFF;
            case ON -> GridConfigSynchronizationType.ON;
            case TWO_WAY -> GridConfigSynchronizationType.TWO_WAY;
        };
    }
}
