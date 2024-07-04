package com.refinedmods.refinedstorage.api.network.impl.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.api.storage.composite.PriorityProvider;

import java.util.Set;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface StorageConfiguration extends PriorityProvider {
    AccessMode getAccessMode();

    boolean isVoidExcess();

    void setVoidExcess(boolean voidExcess);

    void setAccessMode(AccessMode accessMode);

    FilterMode getFilterMode();

    boolean isAllowed(ResourceKey resource);

    void setFilters(Set<ResourceKey> filters);

    void setNormalizer(UnaryOperator<ResourceKey> normalizer);

    void setFilterMode(FilterMode filterMode);

    void setPriority(int priority);

    boolean isActive();
}
