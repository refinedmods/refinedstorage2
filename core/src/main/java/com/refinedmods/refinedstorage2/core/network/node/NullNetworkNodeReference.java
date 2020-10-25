package com.refinedmods.refinedstorage2.core.network.node;

import java.util.Optional;

public class NullNetworkNodeReference implements NetworkNodeReference {
    public static final NetworkNodeReference INSTANCE = new NullNetworkNodeReference();

    @Override
    public Optional<NetworkNode> get() {
        return Optional.empty();
    }
}
