package com.refinedmods.refinedstorage2.platform.api;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PlatformApiProxy implements PlatformApi {
    @Nullable
    private PlatformApi delegate;

    public void setDelegate(final PlatformApi delegate) {
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
    public PlatformStorageRepository getStorageRepository(final Level level) {
        return ensureLoaded().getStorageRepository(level);
    }

    @Override
    public OrderedRegistry<ResourceLocation, StorageChannelType<?>> getStorageChannelTypeRegistry() {
        return ensureLoaded().getStorageChannelTypeRegistry();
    }

    @Override
    public OrderedRegistry<ResourceLocation, ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry() {
        return ensureLoaded().getImporterTransferStrategyRegistry();
    }

    @Override
    public MutableComponent createTranslation(final String category, final String value, final Object... args) {
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
    public void requestNetworkNodeInitialization(final NetworkNodeContainer container,
                                                 final Level level,
                                                 final Runnable callback) {
        ensureLoaded().requestNetworkNodeInitialization(container, level, callback);
    }

    @Override
    public void requestNetworkNodeRemoval(final NetworkNodeContainer container, final Level level) {
        ensureLoaded().requestNetworkNodeRemoval(container, level);
    }

    @Override
    public void requestNetworkNodeUpdate(final NetworkNodeContainer container, final Level level) {
        ensureLoaded().requestNetworkNodeUpdate(container, level);
    }

    private PlatformApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return delegate;
    }
}
