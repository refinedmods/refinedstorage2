package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;

public class FilterModeSettings {
    private static final int BLOCK = 0;
    private static final int ALLOW = 1;

    private FilterModeSettings() {
    }

    public static FilterMode getFilterMode(int filterMode) {
        return switch (filterMode) {
            case BLOCK -> FilterMode.BLOCK;
            case ALLOW -> FilterMode.ALLOW;
            default -> FilterMode.BLOCK;
        };
    }

    public static int getFilterMode(FilterMode filterMode) {
        return switch (filterMode) {
            case BLOCK -> BLOCK;
            case ALLOW -> ALLOW;
        };
    }

    public static TwoWaySyncProperty<FilterMode> createClientSyncProperty(int index) {
        return TwoWaySyncProperty.forClient(
                index,
                FilterModeSettings::getFilterMode,
                FilterModeSettings::getFilterMode,
                FilterMode.BLOCK,
                filterMode -> {
                }
        );
    }
}
