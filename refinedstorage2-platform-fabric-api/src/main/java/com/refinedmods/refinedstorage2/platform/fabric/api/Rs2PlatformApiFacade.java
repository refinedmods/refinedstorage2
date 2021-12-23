package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public interface Rs2PlatformApiFacade {
    Rs2PlatformApiFacade INSTANCE = new Rs2PlatformApiFacadeProxy();

    PlatformStorageRepository getStorageRepository(Level level);

    StorageType<ItemResource> getItemStorageType();

    StorageType<FluidResource> getFluidStorageType();

    TranslatableComponent createTranslation(String category, String value, Object... args);

    ResourceTypeRegistry getResourceTypeRegistry();

    NetworkComponentRegistry getNetworkComponentRegistry();

    void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level, Runnable callback);

    void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level);
}
