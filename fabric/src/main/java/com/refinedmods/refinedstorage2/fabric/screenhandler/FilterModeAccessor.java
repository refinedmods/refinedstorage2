package com.refinedmods.refinedstorage2.fabric.screenhandler;

import com.refinedmods.refinedstorage2.api.stack.filter.FilterMode;

public interface FilterModeAccessor {
    FilterMode getFilterMode();

    void setFilterMode(FilterMode filterMode);
}
