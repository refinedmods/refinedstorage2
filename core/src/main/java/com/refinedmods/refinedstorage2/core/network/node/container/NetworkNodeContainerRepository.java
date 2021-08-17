package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Optional;

public interface NetworkNodeContainerRepository {
    <T extends NetworkNode> Optional<NetworkNodeContainer<T>> getContainer(Position position);
}
