package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.List;

public interface NetworkNodeContainer<T extends NetworkNode> {
    boolean initialize(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry);

    void remove(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry);

    T getNode();

    Position getPosition();

    Rs2World getContainerWorld();

    void setContainerWorld(Rs2World world);

    List<NetworkNodeContainer<?>> getConnections(NetworkNodeContainerRepository containerRepository);

    boolean canConnectWith(NetworkNodeContainer<?> other, Direction incomingDirection);
}
