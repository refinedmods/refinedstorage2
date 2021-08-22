package com.refinedmods.refinedstorage2.core;

import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistryImpl;

public class Rs2CoreApiFacadeImpl implements Rs2CoreApiFacade {
    private final NetworkComponentRegistry networkComponentRegistry = new NetworkComponentRegistryImpl();

    @Override
    public NetworkComponentRegistry getNetworkComponentRegistry() {
        return networkComponentRegistry;
    }
}
