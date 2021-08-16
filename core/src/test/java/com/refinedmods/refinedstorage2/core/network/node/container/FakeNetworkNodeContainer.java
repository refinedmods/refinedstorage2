package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.List;

public class FakeNetworkNodeContainer<T extends NetworkNode> implements NetworkNodeContainer<T> {
    private final T node;
    private final Rs2World world;

    private FakeNetworkNodeContainer(T node, Rs2World world) {
        this.node = node;
        this.world = world;
    }

    public static <T extends NetworkNode> FakeNetworkNodeContainer<T> createForFakeWorld(T node) {
        Rs2World world = new FakeRs2World();
        node.setWorld(world);
        return new FakeNetworkNodeContainer<>(node, world);
    }

    @Override
    public boolean initialize(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T getNode() {
        return node;
    }

    @Override
    public Position getPosition() {
        return Position.ORIGIN;
    }

    @Override
    public Rs2World getContainerWorld() {
        return world;
    }

    @Override
    public void setContainerWorld(Rs2World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NetworkNodeContainer<?>> getConnections(NetworkNodeContainerRepository containerRepository) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canConnectWith(NetworkNodeContainer<?> other, Direction incomingDirection) {
        throw new UnsupportedOperationException();
    }
}
