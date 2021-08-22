package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

public class FakeNetworkNodeContainer<T extends NetworkNode> implements NetworkNodeContainer<T> {
    private final T node;

    public FakeNetworkNodeContainer(T node) {
        this.node = node;
    }

    @Override
    public boolean initialize(ConnectionProvider connectionProvider, NetworkComponentRegistry networkComponentRegistry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ConnectionProvider connectionProvider, NetworkComponentRegistry networkComponentRegistry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T getNode() {
        return node;
    }

    @Override
    public void update() {
        node.update();
    }
}
