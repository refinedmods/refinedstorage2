package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.Rs2World;

public interface NetworkNode {
    Network getNetwork();

    void setNetwork(Network network);

    void update();

    void setWorld(Rs2World world);
}
