package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.util.FilterMode;

public class FilterModeSettings {
    private static final int BLOCK = 0;
    private static final int ALLOW = 1;

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
