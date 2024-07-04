package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneMode;

public interface StorageConfigurationContainer {
    int getPriority();

    void setPriority(int priority);

    FilterMode getFilterMode();

    void setFilterMode(FilterMode filterMode);

    boolean isFuzzyMode();

    void setFuzzyMode(boolean fuzzyMode);

    AccessMode getAccessMode();

    void setAccessMode(AccessMode accessMode);

    boolean isVoidExcess();

    void setVoidExcess(boolean voidExcess);

    RedstoneMode getRedstoneMode();

    void setRedstoneMode(RedstoneMode redstoneMode);
}
