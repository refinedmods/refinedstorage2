package com.refinedmods.refinedstorage2.platform.api;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.item.StorageContainerHelper;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
    public StorageRepository getStorageRepository(final Level level) {
        return ensureLoaded().getStorageRepository(level);
    }

    @Override
    public StorageContainerHelper getStorageContainerHelper() {
        return ensureLoaded().getStorageContainerHelper();
    }

    @Override
    public OrderedRegistry<ResourceLocation, PlatformStorageChannelType<?>> getStorageChannelTypeRegistry() {
        return ensureLoaded().getStorageChannelTypeRegistry();
    }

    @Override
    public OrderedRegistry<ResourceLocation, ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry() {
        return ensureLoaded().getImporterTransferStrategyRegistry();
    }

    @Override
    public OrderedRegistry<ResourceLocation, ExporterTransferStrategyFactory> getExporterTransferStrategyRegistry() {
        return ensureLoaded().getExporterTransferStrategyRegistry();
    }

    @Override
    public <T> void addExternalStorageProviderFactory(final StorageChannelType<T> channelType,
                                                      final int priority,
                                                      final PlatformExternalStorageProviderFactory factory) {
        ensureLoaded().addExternalStorageProviderFactory(channelType, priority, factory);
    }

    @Override
    public <T> Set<PlatformExternalStorageProviderFactory> getExternalStorageProviderFactories(
        final StorageChannelType<T> channelType
    ) {
        return ensureLoaded().getExternalStorageProviderFactories(channelType);
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
    public UpgradeRegistry getUpgradeRegistry() {
        return ensureLoaded().getUpgradeRegistry();
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

    @Override
    public GridInsertionStrategy createGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                                             final Player player,
                                                             final GridServiceFactory serviceFactory) {
        return ensureLoaded().createGridInsertionStrategy(containerMenu, player, serviceFactory);
    }

    @Override
    public void addGridInsertionStrategyFactory(final GridInsertionStrategyFactory insertionStrategyFactory) {
        ensureLoaded().addGridInsertionStrategyFactory(insertionStrategyFactory);
    }

    private PlatformApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return delegate;
    }
}
