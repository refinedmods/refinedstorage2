package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.network.Network;

public interface NetworkNode {
    Network getNetwork();

    void setNetwork(Network network);

    boolean isActive();

    void setActive(boolean active);

    void update();
}
