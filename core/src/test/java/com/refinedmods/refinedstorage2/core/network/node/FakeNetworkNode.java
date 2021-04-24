package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.util.Position;

public class FakeNetworkNode implements NetworkNode {
    private Network network;
    private final Position pos;

    public FakeNetworkNode(Position pos) {
        this.pos = pos;
    }

    @Override
    public Position getPosition() {
        return pos;
    }

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

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void onActiveChanged(boolean active) {

    }

    @Override
    public String toString() {
        return "FakeNetworkNode{" +
                "pos=" + pos +
                '}';
    }
}
