package com.refinedmods.refinedstorage2.core;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;

public interface Rs2CoreApiFacade<W> {
    NetworkComponentRegistry getNetworkComponentRegistry();

    StorageChannelTypeRegistry getStorageChannelTypeRegistry();

    // Move to Rs2PlatformApiFacade
    StorageDiskManager getStorageDiskManager(W world);

    GridSearchBoxModeRegistry getGridSearchBoxModeRegistry();
}