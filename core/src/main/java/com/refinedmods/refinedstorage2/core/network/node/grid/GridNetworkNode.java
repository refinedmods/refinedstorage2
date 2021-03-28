package com.refinedmods.refinedstorage2.core.network.node.grid;

import com.refinedmods.refinedstorage2.core.World;
import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridSortingType;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import net.minecraft.util.math.BlockPos;

public class GridNetworkNode extends NetworkNodeImpl {
    private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;
    private GridSortingType sortingType = GridSortingType.QUANTITY;

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
}
