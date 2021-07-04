package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.List;

public class FakeNetworkNodeContainer<T extends NetworkNode> implements NetworkNodeContainer<T> {
    private final T node;

    public FakeNetworkNodeContainer(T node) {
        this.node = node;
    }

    @Override
    public boolean initialize(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry) {
        return false;
    }

    @Override
    public void remove(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry) {

    }

    @Override
    public T getNode() {
        return node;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public Rs2World getContainerWorld() {
        return null;
    }

    @Override
    public List<NetworkNodeContainer<?>> getConnections(NetworkNodeContainerRepository containerRepository) {
        return null;
    }

    @Override
    public boolean canConnectWith(NetworkNodeContainer<?> other, Direction incomingDirection) {
        return false;
    }
}
