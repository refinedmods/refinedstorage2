package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.storage.AccessMode;

public class AccessModeSettings {
    private static final int INSERT_EXTRACT = 0;
    private static final int INSERT = 1;
    private static final int EXTRACT = 2;

    private AccessModeSettings() {
    }

    public static AccessMode getAccessMode(int accessMode) {
        switch (accessMode) {
            case INSERT_EXTRACT:
                return AccessMode.INSERT_EXTRACT;
            case INSERT:
                return AccessMode.INSERT;
            case EXTRACT:
                return AccessMode.EXTRACT;
            default:
                return AccessMode.INSERT_EXTRACT;
        }
    }

    public static int getAccessMode(AccessMode accessMode) {
        switch (accessMode) {
            case INSERT_EXTRACT:
                return INSERT_EXTRACT;
            case INSERT:
                return INSERT;
            case EXTRACT:
                return EXTRACT;
            default:
                return INSERT_EXTRACT;
        }
    }
}
