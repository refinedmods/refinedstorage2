package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

public enum StorageDiskState {
    NONE(0),
    DISCONNECTED(0x323232),
    NORMAL(0x00E9FF),
    NEAR_CAPACITY(0xFFB700),
    FULL(0xDA4B40);

    private final int color;

    StorageDiskState(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
