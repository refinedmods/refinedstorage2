package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class Rs2PlatformApiFacadeProxy implements Rs2PlatformApiFacade {
    private Rs2PlatformApiFacade facade;

    public void setFacade(Rs2PlatformApiFacade facade) {
        if (this.facade != null) {
            throw new IllegalStateException("Platform API already injected");
        }
        this.facade = facade;
    }

    @Override
    public PlatformStorageRepository getStorageRepository(World world) {
        return ensureLoaded().getStorageRepository(world);
    }

    @Override
    public StorageType<ItemResource> getItemBulkStorageType() {
        return ensureLoaded().getItemBulkStorageType();
    }

    @Override
    public StorageType<FluidResource> getFluidBulkStorageType() {
        return ensureLoaded().getFluidBulkStorageType();
    }

    @Override
    public ConnectionProvider createConnectionProvider(World world) {
        return ensureLoaded().createConnectionProvider(world);
    }

    @Override
    public TranslatableText createTranslation(String category, String value, Object... args) {
        return ensureLoaded().createTranslation(category, value, args);
    }

    private Rs2PlatformApiFacade ensureLoaded() {
        if (facade == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return facade;
    }
}
