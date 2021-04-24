package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.util.Position;

public interface NetworkNode {
    Position getPosition();

    void setNetwork(Network network);

    Network getNetwork();

    NetworkNodeReference createReference();

    boolean isActive();

    void onActiveChanged(boolean active);
}
