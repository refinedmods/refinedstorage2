package com.refinedmods.refinedstorage2.fabric.coreimpl;

import com.refinedmods.refinedstorage2.core.RefinedStorage2ApiFacade;
import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import com.refinedmods.refinedstorage2.core.network.NetworkManagerImpl;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManagerImpl;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.FabricNetworkManager;
import com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk.FabricStorageDiskManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class FabricRefinedStorage2ApiFacade implements RefinedStorage2ApiFacade {
    @Override
    public NetworkManager getNetworkManager(MinecraftServer server) {
        return server
            .getWorld(World.OVERWORLD)
            .getPersistentStateManager()
            .getOrCreate(() -> new FabricNetworkManager(FabricNetworkManager.NAME, new NetworkManagerImpl()), FabricNetworkManager.NAME);
    }

    @Override
    public StorageDiskManager getStorageDiskManager(MinecraftServer server) {
        return server
            .getWorld(World.OVERWORLD)
            .getPersistentStateManager()
            .getOrCreate(() -> new FabricStorageDiskManager(FabricStorageDiskManager.NAME, new StorageDiskManagerImpl()), FabricStorageDiskManager.NAME);
    }
}
