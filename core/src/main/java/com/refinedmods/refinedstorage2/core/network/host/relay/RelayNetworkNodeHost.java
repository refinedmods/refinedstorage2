package com.refinedmods.refinedstorage2.core.network.host.relay;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostImpl;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostRepository;
import com.refinedmods.refinedstorage2.core.network.node.relay.RelayNetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.ArrayList;
import java.util.List;

public class RelayNetworkNodeHost extends NetworkNodeHostImpl<RelayNetworkNode> {
    public RelayNetworkNodeHost(Rs2World world, Position position, RelayNetworkNode node) {
        super(world, position, node);
    }

    @Override
    public List<NetworkNodeHost<?>> getConnections(NetworkNodeHostRepository hostRepository) {
        List<NetworkNodeHost<?>> connections = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction == Direction.NORTH) {
                continue;
            }
            hostRepository.getHost(world, position.offset(direction)).ifPresent(host -> {
                if (host.canConnectWith(this, direction.getOpposite())) {
                    connections.add(host);
                }
            });
        }
        return connections;
    }

    @Override
    public boolean canConnectWith(NetworkNodeHost<?> other, Direction incomingDirection) {
        return incomingDirection != Direction.NORTH;
    }
}
