package com.refinedmods.refinedstorage2.core.network.node.relay;

import com.refinedmods.refinedstorage2.api.core.Direction;
import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;

public class RelayNetworkNode extends NetworkNodeImpl {
    private final Direction direction;

    public RelayNetworkNode(Position position, Direction direction) {
        super(position);
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
