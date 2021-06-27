package com.refinedmods.refinedstorage2.core.network.node.relay;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.util.Position;

public class RelayNetworkNode extends NetworkNodeImpl {
    public RelayNetworkNode(Rs2World world, Position position) {
        super(world, position);
    }

    @Override
    protected long getEnergyUsage() {
        return 0;
    }
}
