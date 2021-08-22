package com.refinedmods.refinedstorage2.core;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistryImpl;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistryImpl;

public class Rs2CoreApiFacadeImpl implements Rs2CoreApiFacade {
    private final GridSearchBoxModeRegistry gridSearchBoxModeRegistry = new GridSearchBoxModeRegistry();
    private final NetworkComponentRegistry networkComponentRegistry = new NetworkComponentRegistryImpl();
    private final StorageChannelTypeRegistry storageChannelTypeRegistry = new StorageChannelTypeRegistryImpl();

    @Override
    public NetworkComponentRegistry getNetworkComponentRegistry() {
        return networkComponentRegistry;
    }

    @Override
    public StorageChannelTypeRegistry getStorageChannelTypeRegistry() {
        return storageChannelTypeRegistry;
    }

    @Override
    public GridSearchBoxModeRegistry getGridSearchBoxModeRegistry() {
        return gridSearchBoxModeRegistry;
    }
}
