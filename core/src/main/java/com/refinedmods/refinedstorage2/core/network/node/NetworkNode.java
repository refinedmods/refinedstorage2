package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.Network;

public interface NetworkNode {
    Network getNetwork();

    void setNetwork(Network network);

    void update();
}
