package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.Network;

public interface NetworkNode {
    void setNetwork(Network network);

    Network getNetwork();

    NetworkNodeReference createReference();
}
