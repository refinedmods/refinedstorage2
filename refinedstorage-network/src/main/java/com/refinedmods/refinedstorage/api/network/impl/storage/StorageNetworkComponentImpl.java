package com.refinedmods.refinedstorage.api.network.impl.storage;

import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageProvider;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannelImpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageNetworkComponentImpl extends StorageChannelImpl implements StorageNetworkComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNetworkComponentImpl.class);

    public StorageNetworkComponentImpl(final ResourceList list) {
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
        return getAll().stream().map(resourceAmount -> new TrackedResourceAmount(
            resourceAmount,
            findTrackedResourceByActorType(resourceAmount.getResource(), actorType).orElse(null)
        )).toList();
    }

    @Override
    public boolean contains(final Storage storage) {
        return this.storage.contains(storage);
    }
}
