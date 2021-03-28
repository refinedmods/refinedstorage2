package com.refinedmods.refinedstorage2.core.network.node.grid;

import java.util.HashSet;
import java.util.Set;

import com.refinedmods.refinedstorage2.core.World;
import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridSortingType;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridNetworkNode extends NetworkNodeImpl {
    private static final Logger LOGGER = LogManager.getLogger(GridNetworkNode.class);

    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private GridSortingType sortingType = GridSortingType.QUANTITY;
    private final Set<GridEventHandler> watchers = new HashSet<>();

    public GridNetworkNode(World world, BlockPos pos, NetworkNodeReference ref) {
        super(world, pos, ref);
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }

    public void setSortingType(GridSortingType sortingType) {
        this.sortingType = sortingType;
    }

    public GridSortingDirection getSortingDirection() {
        return sortingDirection;
    }

    public GridSortingType getSortingType() {
        return sortingType;
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
