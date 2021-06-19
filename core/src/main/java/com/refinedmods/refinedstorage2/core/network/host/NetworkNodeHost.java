package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Position;

public interface NetworkNodeHost<T extends NetworkNode> {
    boolean initialize(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry);

    void remove(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry);

    T getNode();

    Position getPosition();

    Rs2World getHostWorld();
}
