package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.Network;
import net.minecraft.util.math.BlockPos;

public class FakeNetworkNode implements NetworkNode {
    private Network network;
    private final BlockPos pos;

    public FakeNetworkNode(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public BlockPos getPosition() {
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
    public String toString() {
        return "FakeNetworkNode{" +
            "pos=" + pos +
            '}';
    }
}
