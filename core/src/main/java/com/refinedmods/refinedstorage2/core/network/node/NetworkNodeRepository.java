package com.refinedmods.refinedstorage2.core.network.node;

import java.util.Optional;

import com.refinedmods.refinedstorage2.core.util.Position;

public interface NetworkNodeRepository {
    Optional<NetworkNode> getNode(Position pos);
}
