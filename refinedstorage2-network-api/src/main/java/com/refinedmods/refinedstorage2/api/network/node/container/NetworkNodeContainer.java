package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.core.Direction;
import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.api.network.Rs2World;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

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
