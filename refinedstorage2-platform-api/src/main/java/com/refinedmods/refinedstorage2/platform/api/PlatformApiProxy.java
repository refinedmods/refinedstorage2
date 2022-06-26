package com.refinedmods.refinedstorage2.platform.api;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PlatformApiProxy implements PlatformApi {
    private PlatformApi delegate;

    public void setDelegate(PlatformApi delegate) {
        if (this.delegate != null) {
            throw new IllegalStateException("Platform API already injected");
        }
        this.delegate = delegate;
    }

    @Override
    public OrderedRegistry<ResourceLocation, StorageType<?>> getStorageTypeRegistry() {
        return ensureLoaded().getStorageTypeRegistry();
    }

    @Override
    public PlatformStorageRepository getStorageRepository(Level level) {
        return ensureLoaded().getStorageRepository(level);
    }

    @Override
    public OrderedRegistry<ResourceLocation, StorageChannelType<?>> getStorageChannelTypeRegistry() {
        return ensureLoaded().getStorageChannelTypeRegistry();
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
    public MutableComponent createTranslation(String category, String value, Object... args) {
        return ensureLoaded().createTranslation(category, value, args);
    }

    @Override
    public OrderedRegistry<ResourceLocation, ResourceType> getResourceTypeRegistry() {
        return ensureLoaded().getResourceTypeRegistry();
    }

    @Override
    public ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory() {
        return ensureLoaded().getNetworkComponentMapFactory();
    }

    @Override
    public OrderedRegistry<ResourceLocation, GridSynchronizer> getGridSynchronizerRegistry() {
        return ensureLoaded().getGridSynchronizerRegistry();
    }

    @Override
    public void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level, Runnable callback) {
        ensureLoaded().requestNetworkNodeInitialization(container, level, callback);
    }

    @Override
    public void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level) {
        ensureLoaded().requestNetworkNodeRemoval(container, level);
    }

    private PlatformApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return delegate;
    }
}
