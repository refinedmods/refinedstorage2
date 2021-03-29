package com.refinedmods.refinedstorage2.core;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public interface RefinedStorage2ApiFacade {
    NetworkManager getNetworkManager(MinecraftServer server);

    StorageDiskManager getStorageDiskManager(World world);

    GridSearchBoxModeRegistry getGridSearchBoxModeRegistry();
}
