package com.refinedmods.refinedstorage2.platform.api;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageTypeRegistry;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

public interface PlatformApi {
    PlatformApi INSTANCE = new PlatformApiProxy();

    StorageTypeRegistry getStorageTypeRegistry();

    PlatformStorageRepository getStorageRepository(Level level);

    StorageChannelTypeRegistry getStorageChannelTypeRegistry();

    StorageType<ItemResource> getItemStorageType();

    StorageType<FluidResource> getFluidStorageType();

    MutableComponent createTranslation(String category, String value, Object... args);

    ResourceTypeRegistry getResourceTypeRegistry();

    ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory();

    void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level, Runnable callback);

    void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level);
}
