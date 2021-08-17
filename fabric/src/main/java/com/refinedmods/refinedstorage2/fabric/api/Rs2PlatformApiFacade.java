package com.refinedmods.refinedstorage2.fabric.api;

import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;

import net.minecraft.world.World;

public interface Rs2PlatformApiFacade {
    Rs2PlatformApiFacade INSTANCE = new Rs2PlatformApiFacadeImpl();

    StorageDiskManager getStorageDiskManager(World world);
}
