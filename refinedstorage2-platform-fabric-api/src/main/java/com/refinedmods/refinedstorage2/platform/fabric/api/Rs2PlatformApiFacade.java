package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public interface Rs2PlatformApiFacade {
    Rs2PlatformApiFacade INSTANCE = new Rs2PlatformApiFacadeProxy();

    PlatformStorageRepository getStorageRepository(Level level);

    StorageType<ItemResource> getItemStorageType();

    StorageType<FluidResource> getFluidStorageType();

    ConnectionProvider createConnectionProvider(Level level);

    TranslatableComponent createTranslation(String category, String value, Object... args);
}
