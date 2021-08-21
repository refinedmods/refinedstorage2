package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;

import java.util.Objects;

public final class NetworkNodeContainerEntry<T extends NetworkNode> {
    private final NetworkNodeContainer<T> container;
    private final Rs2World world;
    private final Position position;

    public static <T extends NetworkNode> NetworkNodeContainerEntry<T> create(NetworkNodeContainer<T> container) {
        return new NetworkNodeContainerEntry<>(container, container.getContainerWorld(), container.getPosition());
    }

    private NetworkNodeContainerEntry(NetworkNodeContainer<T> container, Rs2World world, Position position) {
        this.container = container;
        this.world = world;
        this.position = position;
    }

    public NetworkNodeContainer<T> getContainer() {
        return container;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkNodeContainerEntry that = (NetworkNodeContainerEntry) o;
        return Objects.equals(world, that.world) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, position);
    }
}
