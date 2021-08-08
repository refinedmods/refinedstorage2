package com.refinedmods.refinedstorage2.core.network.node.relay;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

public class RelayNetworkNode extends NetworkNodeImpl {
    private final Direction direction;

    public RelayNetworkNode(Rs2World world, Position position, Direction direction) {
        super(world, position);
        this.direction = direction;
    }

    @Override
    protected long getEnergyUsage() {
        return 0;
    }

    public Direction getDirection() {
        return direction;
    }
}
