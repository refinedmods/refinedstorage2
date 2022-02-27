package com.refinedmods.refinedstorage2.platform.api.grid;

public enum GridSynchronizationType {
    OFF,
    ON,
    TWO_WAY;

    public GridSynchronizationType toggle() {
        return switch (this) {
            case OFF -> ON;
            case ON -> TWO_WAY;
            case TWO_WAY -> OFF;
        };
    }
}
