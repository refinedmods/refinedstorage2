package com.refinedmods.refinedstorage2.fabric.coreimpl;

import com.refinedmods.refinedstorage2.core.Rs2ApiFacade;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistryImpl;
import com.refinedmods.refinedstorage2.core.storage.disk.ClientStorageDiskManager;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManagerImpl;
import com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk.FabricRequestInfoCallback;
import com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk.FabricStorageDiskManager;

import net.minecraft.world.World;

public class FabricRs2ApiFacade implements Rs2ApiFacade<World> {
    private final StorageDiskManager clientStorageDiskManager = new ClientStorageDiskManager(new FabricRequestInfoCallback());
    private final GridSearchBoxModeRegistry gridSearchBoxModeRegistry = new GridSearchBoxModeRegistry();
    private final NetworkComponentRegistry networkComponentRegistry = new NetworkComponentRegistryImpl();

    @Override
    public NetworkComponentRegistry getNetworkComponentRegistry() {
        return networkComponentRegistry;
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
