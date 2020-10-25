package com.refinedmods.refinedstorage2.core.network.node;

import java.util.Optional;

@FunctionalInterface
public interface NetworkNodeReference {
    Optional<NetworkNode> get();
}
