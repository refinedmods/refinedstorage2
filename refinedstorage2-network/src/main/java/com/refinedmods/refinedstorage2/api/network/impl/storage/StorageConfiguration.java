package com.refinedmods.refinedstorage2.api.network.impl.storage;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.composite.Priority;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface StorageConfiguration extends Priority {
    AccessMode getAccessMode();

    default boolean isInsertOnly() {
        return getAccessMode() == AccessMode.INSERT;
    }

    default boolean isExtractOnly() {
        return getAccessMode() == AccessMode.EXTRACT;
    }

    boolean isVoidExcess();

    void setVoidExcess(boolean voidExcess);

    void setAccessMode(AccessMode accessMode);

    FilterMode getFilterMode();

    boolean isAllowed(ResourceKey resource);

    void setFilterMode(FilterMode filterMode);

    void setPriority(int priority);

    boolean isActive();
}
