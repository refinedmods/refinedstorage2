package com.refinedmods.refinedstorage2.api.network.node.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridNetworkNode<T> extends NetworkNodeImpl {
    private static final Logger LOGGER = LogManager.getLogger(GridNetworkNode.class);

    private final Set<GridWatcher> watchers = new HashSet<>();
    private final long energyUsage;
    private final StorageChannelType<T> type;

    public GridNetworkNode(long energyUsage, StorageChannelType<T> type) {
        this.energyUsage = energyUsage;
        this.type = type;
    }

    public StorageChannel<T> getStorageChannel() {
        return network.getComponent(StorageNetworkComponent.class).getStorageChannel(type);
    }

    public int getResourceCount() {
        return getStorageChannel().getAll().size();
    }

    public void forEachResource(BiConsumer<ResourceAmount<T>, Optional<StorageTracker.Entry>> consumer) {
        StorageChannel<T> storageChannel = getStorageChannel();
        storageChannel.getAll().forEach(resourceAmount -> consumer.accept(resourceAmount, storageChannel.getTracker().getEntry(resourceAmount.getResource())));
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    public void addWatcher(GridWatcher watcher) {
        watchers.add(watcher);
        LOGGER.info("Watcher was added, new count is {}", watchers.size());
    }

    public void removeWatcher(GridWatcher watcher) {
        watchers.remove(watcher);
        LOGGER.info("Watcher was removed, new count is {}", watchers.size());
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        watchers.forEach(watcher -> watcher.onActiveChanged(active));
    }
}
