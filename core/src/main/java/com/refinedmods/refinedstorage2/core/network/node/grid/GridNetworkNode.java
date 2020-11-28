package com.refinedmods.refinedstorage2.core.network.node.grid;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import net.minecraft.util.math.BlockPos;

public class GridNetworkNode extends NetworkNodeImpl {
    public GridNetworkNode(BlockPos pos, NetworkNodeReference ref) {
        super(pos, ref);
    }
}
