package com.refinedmods.refinedstorage2.api.network.impl.component;

import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;

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
}
