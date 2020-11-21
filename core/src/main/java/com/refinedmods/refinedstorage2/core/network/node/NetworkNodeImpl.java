package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.Network;
import net.minecraft.util.math.BlockPos;

public class NetworkNodeImpl implements NetworkNode {
    private final BlockPos pos;
    private final NetworkNodeReference ref;
    protected Network network;

    public NetworkNodeImpl(BlockPos pos, NetworkNodeReference ref) {
        this.pos = pos;
        this.ref = ref;
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
        return ref;
    }
}
