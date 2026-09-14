package com.refinedmods.refinedstorage.api.network.impl.storage;

import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageProvider;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.root.RootStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageNetworkComponentImpl extends RootStorageImpl implements StorageNetworkComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNetworkComponentImpl.class);

    public StorageNetworkComponentImpl(final MutableResourceList list) {
        super(list);
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof StorageProvider provider) {
            final Storage storage = provider.getStorage();
            LOGGER.debug("Adding source {} from provider {}", storage, provider);
            addSource(storage);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof StorageProvider provider) {
            final Storage storage = provider.getStorage();
            LOGGER.debug("Removing source {} of provider {}", storage, provider);
            removeSource(storage);
        }
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        final Collection<ResourceAmount> all = getAll();
        final Map<ResourceKey, TrackedResource> trackedResources = getTrackedResourcesByActorType(
            actorType,
            all.stream().map(ResourceAmount::resource).collect(Collectors.toSet())
        );
        return all.stream().map(resourceAmount -> new TrackedResourceAmount(
            resourceAmount,
            trackedResources.get(resourceAmount.resource())
        )).toList();
    }

    @Override
    public boolean contains(final Storage storage) {
        return this.storage.contains(storage);
    }
}
