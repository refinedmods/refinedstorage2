package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;

public class AccessModeSettings {
    private static final int INSERT_EXTRACT = 0;
    private static final int INSERT = 1;
    private static final int EXTRACT = 2;

    private AccessModeSettings() {
    }

    public static AccessMode getAccessMode(int accessMode) {
        return switch (accessMode) {
            case INSERT_EXTRACT -> AccessMode.INSERT_EXTRACT;
            case INSERT -> AccessMode.INSERT;
            case EXTRACT -> AccessMode.EXTRACT;
            default -> AccessMode.INSERT_EXTRACT;
        };
    }

    public static int getAccessMode(AccessMode accessMode) {
        return switch (accessMode) {
            case INSERT_EXTRACT -> INSERT_EXTRACT;
            case INSERT -> INSERT;
            case EXTRACT -> EXTRACT;
        };
    }

    public static TwoWaySyncProperty<AccessMode> createClientSyncProperty(int index) {
        return TwoWaySyncProperty.forClient(
                index,
                AccessModeSettings::getAccessMode,
                AccessModeSettings::getAccessMode,
                AccessMode.INSERT_EXTRACT,
                accessMode -> {
                }
        );
    }
}
