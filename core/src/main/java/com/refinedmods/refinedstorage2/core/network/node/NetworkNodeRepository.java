package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Optional;

public interface NetworkNodeRepository {
    Optional<NetworkNode> getNode(Position pos);
}
