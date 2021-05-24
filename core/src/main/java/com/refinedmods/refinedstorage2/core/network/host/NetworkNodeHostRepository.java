package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Optional;

public interface NetworkNodeHostRepository {
    <T extends NetworkNode> Optional<NetworkNodeHost<T>> getHost(Rs2World world, Position position);
}
