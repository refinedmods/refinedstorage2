package com.refinedmods.refinedstorage.api.network.impl.storage;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;

import java.util.Set;
import java.util.function.UnaryOperator;

public class NetworkNodeStorageConfiguration implements StorageConfiguration {
    private final AbstractNetworkNode node;
    private final Filter filter = new Filter();

    private int priority;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private boolean voidExcess;

    public NetworkNodeStorageConfiguration(final AbstractNetworkNode node) {
        this.node = node;
    }

    @Override
    public AccessMode getAccessMode() {
        return accessMode;
    }

    @Override
    public boolean isVoidExcess() {
        return voidExcess;
    }

    @Override
    public void setVoidExcess(final boolean voidExcess) {
        this.voidExcess = voidExcess;
    }

    @Override
    public void setAccessMode(final AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    @Override
    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    @Override
    public boolean isAllowed(final ResourceKey resource) {
        return filter.isAllowed(resource);
    }

    @Override
    public void setFilters(final Set<ResourceKey> filters) {
        filter.setFilters(filters);
    }

    @Override
    public void setNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        filter.setNormalizer(normalizer);
    }

    @Override
    public void setFilterMode(final FilterMode filterMode) {
        filter.setMode(filterMode);
    }

    @Override
    public void setPriority(final int priority) {
        this.priority = priority;
        trySortSources();
    }

    private void trySortSources() {
        final Network network = node.getNetwork();
        if (network == null) {
            return;
        }
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        storage.sortSources();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isActive() {
        return node.isActive();
    }
}
