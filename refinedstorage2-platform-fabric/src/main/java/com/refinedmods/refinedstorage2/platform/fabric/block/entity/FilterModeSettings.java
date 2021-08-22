package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.stack.filter.FilterMode;

public class FilterModeSettings {
    private static final int BLOCK = 0;
    private static final int ALLOW = 1;

    private FilterModeSettings() {
    }

    public static FilterMode getFilterMode(int filterMode) {
        switch (filterMode) {
            case BLOCK:
                return FilterMode.BLOCK;
            case ALLOW:
                return FilterMode.ALLOW;
            default:
                return FilterMode.BLOCK;
        }
    }

    public static int getFilterMode(FilterMode filterMode) {
        switch (filterMode) {
            case BLOCK:
                return BLOCK;
            case ALLOW:
                return ALLOW;
            default:
                return BLOCK;
        }
    }
}
