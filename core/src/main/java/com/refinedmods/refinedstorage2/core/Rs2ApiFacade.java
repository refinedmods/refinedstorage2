package com.refinedmods.refinedstorage2.core;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;

public interface Rs2ApiFacade<S, W> {
    NetworkManager getNetworkManager(S server);

    StorageDiskManager getStorageDiskManager(W world);

    GridSearchBoxModeRegistry getGridSearchBoxModeRegistry();
}
