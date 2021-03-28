package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.Network;
import net.minecraft.util.math.BlockPos;

public interface NetworkNode {
    BlockPos getPosition();

    void setNetwork(Network network);

    Network getNetwork();

    NetworkNodeReference createReference();

    boolean isActive();
}
