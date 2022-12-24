package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.storage.Actor;

public class NetworkNodeActor implements Actor {
    private final NetworkNode networkNode;

    public NetworkNodeActor(final NetworkNode networkNode) {
        this.networkNode = networkNode;
    }

    public NetworkNode getNetworkNode() {
        return networkNode;
    }

    @Override
    public String getName() {
        return networkNode.getClass().getName();
    }
}
