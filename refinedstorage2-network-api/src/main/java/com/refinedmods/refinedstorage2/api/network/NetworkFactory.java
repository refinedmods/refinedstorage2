package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;

public class NetworkFactory {
    private final NetworkComponentRegistry componentRegistry;

    public NetworkFactory(NetworkComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    public Network create() {
        return new NetworkImpl(componentRegistry);
    }
}
