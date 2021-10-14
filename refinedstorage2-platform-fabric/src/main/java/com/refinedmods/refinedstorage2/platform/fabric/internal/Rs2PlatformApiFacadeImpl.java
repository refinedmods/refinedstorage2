package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.storage.StorageManagerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.FabricConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricClientStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.FluidBulkStorageType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.ItemBulkStorageType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class Rs2PlatformApiFacadeImpl implements Rs2PlatformApiFacade {
    private final PlatformStorageManager clientStorageManager = new FabricClientStorageManager();

    @Override
    public PlatformStorageManager getStorageManager(World world) {
        if (world.getServer() == null) {
            return clientStorageManager;
        }

        return world
                .getServer()
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager()
                .getOrCreate(this::createStorageManager, this::createStorageManager, FabricStorageManager.NAME);
    }

    @Override
    public StorageType<ItemResource> getItemBulkStorageType() {
        return ItemBulkStorageType.INSTANCE;
    }

    @Override
    public StorageType<FluidResource> getFluidBulkStorageType() {
        return FluidBulkStorageType.INSTANCE;
    }

    @Override
    public ConnectionProvider createConnectionProvider(World world) {
        return new FabricConnectionProvider(world);
    }

    @Override
    public TranslatableText createTranslation(String category, String value, Object... args) {
        return Rs2Mod.createTranslation(category, value, args);
    }

    private FabricStorageManager createStorageManager(NbtCompound tag) {
        var manager = createStorageManager();
        manager.read(tag);
        return manager;
    }

    private FabricStorageManager createStorageManager() {
        return new FabricStorageManager(new StorageManagerImpl());
    }
}
