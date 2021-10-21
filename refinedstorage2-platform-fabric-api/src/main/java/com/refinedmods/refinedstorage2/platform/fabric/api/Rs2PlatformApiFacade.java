package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public interface Rs2PlatformApiFacade {
    Rs2PlatformApiFacade INSTANCE = new Rs2PlatformApiFacadeProxy();

    PlatformStorageRepository getStorageRepository(World world);

    StorageType<ItemResource> getItemBulkStorageType();

    StorageType<FluidResource> getFluidBulkStorageType();

    ConnectionProvider createConnectionProvider(World world);

    TranslatableText createTranslation(String category, String value, Object... args);
}
