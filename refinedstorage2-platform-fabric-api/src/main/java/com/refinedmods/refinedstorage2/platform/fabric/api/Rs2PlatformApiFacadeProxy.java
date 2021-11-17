package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class Rs2PlatformApiFacadeProxy implements Rs2PlatformApiFacade {
    private Rs2PlatformApiFacade facade;

    public void setFacade(Rs2PlatformApiFacade facade) {
        if (this.facade != null) {
            throw new IllegalStateException("Platform API already injected");
        }
        this.facade = facade;
    }

    @Override
    public PlatformStorageRepository getStorageRepository(Level level) {
        return ensureLoaded().getStorageRepository(level);
    }

    @Override
    public StorageType<ItemResource> getItemStorageType() {
        return ensureLoaded().getItemStorageType();
    }

    @Override
    public StorageType<FluidResource> getFluidStorageType() {
        return ensureLoaded().getFluidStorageType();
    }

    @Override
    public ConnectionProvider createConnectionProvider(Level level) {
        return ensureLoaded().createConnectionProvider(level);
    }

    @Override
    public TranslatableComponent createTranslation(String category, String value, Object... args) {
        return ensureLoaded().createTranslation(category, value, args);
    }

    private Rs2PlatformApiFacade ensureLoaded() {
        if (facade == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return facade;
    }
}
