package com.refinedmods.refinedstorage2.fabric.coreimpl;

import com.refinedmods.refinedstorage2.core.Rs2ApiFacade;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import com.refinedmods.refinedstorage2.core.network.NetworkManagerImpl;
import com.refinedmods.refinedstorage2.core.storage.disk.ClientStorageDiskManager;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManagerImpl;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.FabricNetworkManager;
import com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk.FabricRequestInfoCallback;
import com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk.FabricStorageDiskManager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class FabricRs2ApiFacade implements Rs2ApiFacade<MinecraftServer, World> {
    private final StorageDiskManager clientStorageDiskManager = new ClientStorageDiskManager(new FabricRequestInfoCallback());
    private final GridSearchBoxModeRegistry gridSearchBoxModeRegistry = new GridSearchBoxModeRegistry();

    @Override
    public NetworkManager getNetworkManager(MinecraftServer server) {
        return server
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager()
                .getOrCreate(() -> new FabricNetworkManager(FabricNetworkManager.NAME, new NetworkManagerImpl()), FabricNetworkManager.NAME);
    }

    @Override
    public StorageDiskManager getStorageDiskManager(World world) {
        if (world.getServer() == null) {
            return clientStorageDiskManager;
        }

        return world
                .getServer()
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager()
                .getOrCreate(() -> new FabricStorageDiskManager(FabricStorageDiskManager.NAME, new StorageDiskManagerImpl()), FabricStorageDiskManager.NAME);
    }

    @Override
    public GridSearchBoxModeRegistry getGridSearchBoxModeRegistry() {
        return gridSearchBoxModeRegistry;
    }
}
