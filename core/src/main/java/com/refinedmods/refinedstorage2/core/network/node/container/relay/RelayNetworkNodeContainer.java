package com.refinedmods.refinedstorage2.core.network.node.container.relay;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainerRepository;
import com.refinedmods.refinedstorage2.core.network.node.relay.RelayNetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.ArrayList;
import java.util.List;

public class RelayNetworkNodeContainer extends NetworkNodeContainerImpl<RelayNetworkNode> {
    public RelayNetworkNodeContainer(Rs2World world, Position position, RelayNetworkNode node) {
        super(world, position, node);
    }

    @Override
    public List<NetworkNodeContainer<?>> getConnections(NetworkNodeContainerRepository containerRepository) {
        List<NetworkNodeContainer<?>> connections = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction == Direction.NORTH) {
                continue;
            }
            containerRepository.getContainer(world, position.offset(direction)).ifPresent(container -> {
                if (container.canConnectWith(this, direction.getOpposite())) {
                    connections.add(container);
                }
            });
        }
        return connections;
    }

    @Override
    public boolean canConnectWith(NetworkNodeContainer<?> other, Direction incomingDirection) {
        return incomingDirection != Direction.NORTH;
    }
}
