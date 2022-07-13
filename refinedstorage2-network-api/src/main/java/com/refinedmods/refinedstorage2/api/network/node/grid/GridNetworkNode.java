package com.refinedmods.refinedstorage2.api.network.node.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridNetworkNode<T> extends AbstractNetworkNode {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Set<GridWatcher> watchers = new HashSet<>();
    private final long energyUsage;
    private final StorageChannelType<T> type;

    public GridNetworkNode(final long energyUsage, final StorageChannelType<T> type) {
        this.energyUsage = energyUsage;
        this.type = type;
    }

    public StorageChannel<T> getStorageChannel() {
        if (network == null) {
            throw new IllegalStateException("Network must be present to retrieve storage channel");
        }
        return network.getComponent(StorageNetworkComponent.class).getStorageChannel(type);
    }

    public int getResourceAmount() {
        return getStorageChannel().getAll().size();
    }

    public void forEachResource(final BiConsumer<ResourceAmount<T>, Optional<TrackedResource>> consumer,
                                final Class<? extends Actor> sourceType) {
        final StorageChannel<T> storageChannel = getStorageChannel();
        storageChannel.getAll().forEach(resourceAmount -> consumer.accept(
            resourceAmount,
            storageChannel.findTrackedResourceBySourceType(resourceAmount.getResource(), sourceType)
        ));
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public void addWatcher(final GridWatcher watcher) {
        watchers.add(watcher);
        LOGGER.info("Watcher was added, new count is {}", watchers.size());
    }

    public void removeWatcher(final GridWatcher watcher) {
        watchers.remove(watcher);
        LOGGER.info("Watcher was removed, new count is {}", watchers.size());
    }

    @Override
    public void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        watchers.forEach(watcher -> watcher.onActiveChanged(newActive));
    }
}
