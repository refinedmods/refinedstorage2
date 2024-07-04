package com.refinedmods.refinedstorage.api.network.impl;

import com.refinedmods.refinedstorage.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.NetworkComponent;

public class NetworkFactory {
    private final ComponentMapFactory<NetworkComponent, Network> componentMapFactory;

    public NetworkFactory(final ComponentMapFactory<NetworkComponent, Network> componentMapFactory) {
        this.componentMapFactory = componentMapFactory;
    }

    public Network create() {
        return new NetworkImpl(componentMapFactory);
    }
}
