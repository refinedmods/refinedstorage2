package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;

public interface StorageConfiguration extends Priority {
    AccessMode getAccessMode();

    void setAccessMode(AccessMode accessMode);

    FilterMode getFilterMode();

    void setFilterMode(FilterMode filterMode);

    void setPriority(int priority);
}
