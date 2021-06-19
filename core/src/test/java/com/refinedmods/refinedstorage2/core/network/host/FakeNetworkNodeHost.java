package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Position;

public class FakeNetworkNodeHost<T extends NetworkNode> implements NetworkNodeHost<T> {
    private final T node;

    public FakeNetworkNodeHost(T node) {
        this.node = node;
    }

    @Override
    public boolean initialize(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {
        return false;
    }

    @Override
    public void remove(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {

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
    public Rs2World getHostWorld() {
        return null;
    }
}
