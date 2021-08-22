package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

public interface NetworkNodeContainer<T extends NetworkNode> {
    boolean initialize(ConnectionProvider connectionProvider, NetworkComponentRegistry networkComponentRegistry);

    void remove(ConnectionProvider connectionProvider, NetworkComponentRegistry networkComponentRegistry);

    T getNode();

    void update();
}
