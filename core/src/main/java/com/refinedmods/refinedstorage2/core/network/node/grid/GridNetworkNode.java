package com.refinedmods.refinedstorage2.core.network.node.grid;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.grid.GridSize;
import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridSortingType;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridNetworkNode extends NetworkNodeImpl {
    private static final Logger LOGGER = LogManager.getLogger(GridNetworkNode.class);
    private final Set<GridEventHandler> watchers = new HashSet<>();
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private GridSortingType sortingType = GridSortingType.QUANTITY;
    private GridSize size = GridSize.STRETCH;
    private GridSearchBoxMode searchBoxMode;

    public GridNetworkNode(Rs2World world, Position pos, NetworkNodeReference ref, GridSearchBoxModeRegistry searchBoxModeRegistry) {
        super(world, pos, ref);
        this.searchBoxMode = searchBoxModeRegistry.getDefault();
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

    public void addWatcher(GridEventHandler watcher) {
        watchers.add(watcher);
        LOGGER.info("Watcher was added, new count is {}", watchers.size());
    }

    public void removeWatcher(GridEventHandler watcher) {
        watchers.remove(watcher);
        LOGGER.info("Watcher was removed, new count is {}", watchers.size());
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        watchers.forEach(watcher -> watcher.onActiveChanged(active));
    }
}
