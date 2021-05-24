package com.refinedmods.refinedstorage2.core;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;

public interface Rs2ApiFacade<W> {
    NetworkComponentRegistry getNetworkComponentRegistry();

    StorageDiskManager getStorageDiskManager(W world);

    GridSearchBoxModeRegistry getGridSearchBoxModeRegistry();
}
