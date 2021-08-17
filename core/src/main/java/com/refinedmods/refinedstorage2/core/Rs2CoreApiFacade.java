package com.refinedmods.refinedstorage2.core;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelTypeRegistry;

public interface Rs2CoreApiFacade {
    Rs2CoreApiFacade INSTANCE = new Rs2CoreApiFacadeImpl();

    NetworkComponentRegistry getNetworkComponentRegistry();

    StorageChannelTypeRegistry getStorageChannelTypeRegistry();

    GridSearchBoxModeRegistry getGridSearchBoxModeRegistry();
}
