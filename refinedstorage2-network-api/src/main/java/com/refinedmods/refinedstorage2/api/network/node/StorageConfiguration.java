package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface StorageConfiguration extends Priority {
    AccessMode getAccessMode();

    void setAccessMode(AccessMode accessMode);

    FilterMode getFilterMode();

    boolean isAllowed(Object resource);

    void setFilterMode(FilterMode filterMode);

    void setPriority(int priority);

    boolean isActive();
}
