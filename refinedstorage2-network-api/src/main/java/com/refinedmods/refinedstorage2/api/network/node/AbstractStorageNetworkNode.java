package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Set;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public abstract class AbstractStorageNetworkNode extends AbstractNetworkNode implements StorageConfiguration {
    private final Filter filter = new Filter();
    private int priority;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public AccessMode getAccessMode() {
        return accessMode;
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
    public boolean isAllowed(final Object resource) {
        return filter.isAllowed(resource);
    }

    @Override
    public void setFilterMode(final FilterMode mode) {
        filter.setMode(mode);
    }

    @Override
    public void setPriority(final int priority) {
        this.priority = priority;

        trySortSources();
    }

    private void trySortSources() {
        if (network == null) {
            return;
        }
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        getRelevantStorageChannelTypes().forEach(type -> storage.getStorageChannel(type).sortSources());
    }

    protected abstract Set<StorageChannelType<?>> getRelevantStorageChannelTypes();

    public void setFilterTemplates(final Set<Object> templates) {
        filter.setTemplates(templates);
    }

    public void setNormalizer(final UnaryOperator<Object> normalizer) {
        filter.setNormalizer(normalizer);
    }
}
