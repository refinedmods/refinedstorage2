package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

import java.util.Optional;

public interface NetworkNodeContainerRepository {
    <T extends NetworkNode> Optional<NetworkNodeContainer<T>> getContainer(Position position);
}
