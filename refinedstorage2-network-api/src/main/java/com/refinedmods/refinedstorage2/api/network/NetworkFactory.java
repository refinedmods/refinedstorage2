package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;

public class NetworkFactory {
    private final ComponentMapFactory<NetworkComponent, Network> componentMapFactory;

    public NetworkFactory(ComponentMapFactory<NetworkComponent, Network> componentMapFactory) {
        this.componentMapFactory = componentMapFactory;
    }

    public Network create() {
        return new NetworkImpl(componentMapFactory);
    }
}
