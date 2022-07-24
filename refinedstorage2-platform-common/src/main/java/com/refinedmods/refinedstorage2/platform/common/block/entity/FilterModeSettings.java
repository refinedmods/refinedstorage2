package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;

public class FilterModeSettings {
    private static final int BLOCK = 0;
    private static final int ALLOW = 1;

    private FilterModeSettings() {
    }

    public static FilterMode getFilterMode(final int filterMode) {
        return switch (filterMode) {
            case BLOCK -> FilterMode.BLOCK;
            case ALLOW -> FilterMode.ALLOW;
            default -> FilterMode.BLOCK;
        };
    }

    public static int getFilterMode(final FilterMode filterMode) {
        return switch (filterMode) {
            case BLOCK -> BLOCK;
            case ALLOW -> ALLOW;
        };
    }
}
