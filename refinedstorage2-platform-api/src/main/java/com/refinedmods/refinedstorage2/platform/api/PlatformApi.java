package com.refinedmods.refinedstorage2.platform.api;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public interface PlatformApi {
    PlatformApi INSTANCE = new PlatformApiProxy();

    PlatformStorageRepository getStorageRepository(Level level);

    StorageType<ItemResource> getItemStorageType();

    StorageType<FluidResource> getFluidStorageType();

    TranslatableComponent createTranslation(String category, String value, Object... args);

    ResourceTypeRegistry getResourceTypeRegistry();

    ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory();

    void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level, Runnable callback);

    void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level);
}
