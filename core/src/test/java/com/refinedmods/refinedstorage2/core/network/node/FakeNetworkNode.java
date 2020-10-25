package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.Network;

public class FakeNetworkNode implements NetworkNode {
    private Network network;

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public NetworkNodeReference createReference() {
        return new StubNetworkNodeReference(this);
    }
}
