package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FakeNetworkNodeContainerRepository implements NetworkNodeContainerRepository {
    private final Table<Rs2World, Position, NetworkNodeContainer<?>> containers = HashBasedTable.create();

    public static FakeNetworkNodeContainerRepository of(NetworkNodeContainer<?>... containers) {
        FakeNetworkNodeContainerRepository repo = new FakeNetworkNodeContainerRepository();
        for (NetworkNodeContainer<?> container : containers) {
            repo.setContainer(container.getContainerWorld(), container.getPosition(), container);
        }
        return repo;
    }

    public void removeContainer(Rs2World world, Position position) {
        containers.remove(world, position);
    }

    public FakeNetworkNodeContainerRepository setContainer(Rs2World world, Position position, NetworkNodeContainer<?> container) {
        containers.put(world, position, container);
        return this;
    }

    @Override
    public <T extends NetworkNode> Optional<NetworkNodeContainer<T>> getContainer(Rs2World world, Position position) {
        NetworkNodeContainer<?> container = containers.get(world, position);
        return Optional.ofNullable(container == null ? null : (NetworkNodeContainer<T>) container);
    }
}
