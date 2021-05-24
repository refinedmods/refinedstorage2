package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

public class EmptyNetworkNode extends NetworkNodeImpl {
    public EmptyNetworkNode(Rs2World world, Position position) {
        super(world, position);
    }

    @Override
    protected long getEnergyUsage() {
        return 0;
    }
}
