package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeNetworkNodeContainerRepository implements NetworkNodeContainerRepository {
    private final Map<Position, NetworkNodeContainer<?>> containers = new HashMap<>();

    public static FakeNetworkNodeContainerRepository of(NetworkNodeContainer<?>... containers) {
        FakeNetworkNodeContainerRepository repo = new FakeNetworkNodeContainerRepository();
        for (NetworkNodeContainer<?> container : containers) {
            repo.setContainer(container.getPosition(), container);
        }
        return repo;
    }

    public void removeContainer(Position position) {
        containers.remove(position);
    }

    public FakeNetworkNodeContainerRepository setContainer(Position position, NetworkNodeContainer<?> container) {
        containers.put(position, container);
        return this;
    }

    @Override
    public <T extends NetworkNode> Optional<NetworkNodeContainer<T>> getContainer(Position position) {
        NetworkNodeContainer<?> container = containers.get(position);
        return Optional.ofNullable(container == null ? null : (NetworkNodeContainer<T>) container);
    }
}
