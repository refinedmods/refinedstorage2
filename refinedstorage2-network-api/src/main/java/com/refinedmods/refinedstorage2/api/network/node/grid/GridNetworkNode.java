package com.refinedmods.refinedstorage2.api.network.node.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridSize;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
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
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private GridSortingType sortingType = GridSortingType.QUANTITY;
    private GridSize size = GridSize.STRETCH;
    private GridSearchBoxMode searchBoxMode;
    private final long energyUsage;
    private final StorageChannelType<T> type;

    public GridNetworkNode(GridSearchBoxMode defaultSearchBoxMode, long energyUsage, StorageChannelType<T> type) {
        this.searchBoxMode = defaultSearchBoxMode;
        this.energyUsage = energyUsage;
        this.type = type;
    }

    public GridSortingDirection getSortingDirection() {
        return sortingDirection;
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    public GridSortingType getSortingType() {
        return sortingType;
    }

    public void setSortingType(GridSortingType sortingType) {
        this.sortingType = sortingType;
    }

    public GridSize getSize() {
        return size;
    }

    public void setSize(GridSize size) {
        this.size = size;
    }

    public GridSearchBoxMode getSearchBoxMode() {
        return searchBoxMode;
    }

    public void setSearchBoxMode(GridSearchBoxMode searchBoxMode) {
        this.searchBoxMode = searchBoxMode;
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
    public void setActive(boolean active) {
        super.setActive(active);
        watchers.forEach(watcher -> watcher.onActiveChanged(active));
    }
}
