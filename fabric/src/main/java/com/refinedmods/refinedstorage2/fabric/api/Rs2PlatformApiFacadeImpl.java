package com.refinedmods.refinedstorage2.fabric.api;

import com.refinedmods.refinedstorage2.api.storage.disk.ClientStorageDiskManager;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskManager;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskManagerImpl;
import com.refinedmods.refinedstorage2.fabric.api.storage.disk.FabricRequestInfoCallback;
import com.refinedmods.refinedstorage2.fabric.api.storage.disk.FabricStorageDiskManager;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class Rs2PlatformApiFacadeImpl implements Rs2PlatformApiFacade {
    private final StorageDiskManager clientStorageDiskManager = new ClientStorageDiskManager(new FabricRequestInfoCallback());

    @Override
    public StorageDiskManager getStorageDiskManager(World world) {
        if (world.getServer() == null) {
            return clientStorageDiskManager;
        }

        return world
                .getServer()
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager()
                .getOrCreate(this::createStorageDiskManager, this::createStorageDiskManager, FabricStorageDiskManager.NAME);
    }

    private FabricStorageDiskManager createStorageDiskManager(NbtCompound tag) {
        var manager = createStorageDiskManager();
        manager.read(tag);
        return manager;
    }

    private FabricStorageDiskManager createStorageDiskManager() {
        return new FabricStorageDiskManager(new StorageDiskManagerImpl());
    }
}
