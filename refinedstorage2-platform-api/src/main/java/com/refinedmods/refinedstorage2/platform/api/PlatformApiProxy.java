package com.refinedmods.refinedstorage2.platform.api;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.blockentity.constructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.blockentity.destructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.item.StorageContainerHelper;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.Collection;
import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;
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
    public PlatformRegistry<StorageType<?>> getStorageTypeRegistry() {
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
    public PlatformRegistry<PlatformStorageChannelType<?>> getStorageChannelTypeRegistry() {
        return ensureLoaded().getStorageChannelTypeRegistry();
    }

    @Override
    public PlatformRegistry<ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry() {
        return ensureLoaded().getImporterTransferStrategyRegistry();
    }

    @Override
    public PlatformRegistry<ExporterTransferStrategyFactory> getExporterTransferStrategyRegistry() {
        return ensureLoaded().getExporterTransferStrategyRegistry();
    }

    @Override
    public <T> void addExternalStorageProviderFactory(final StorageChannelType<T> channelType,
                                                      final PlatformExternalStorageProviderFactory factory) {
        ensureLoaded().addExternalStorageProviderFactory(channelType, factory);
    }

    @Override
    public <T> Collection<PlatformExternalStorageProviderFactory> getExternalStorageProviderFactories(
        final StorageChannelType<T> channelType
    ) {
        return ensureLoaded().getExternalStorageProviderFactories(channelType);
    }

    @Override
    public Collection<DestructorStrategyFactory> getDestructorStrategyFactories() {
        return ensureLoaded().getDestructorStrategyFactories();
    }

    @Override
    public void addDestructorStrategyFactory(final DestructorStrategyFactory factory) {
        ensureLoaded().addDestructorStrategyFactory(factory);
    }

    @Override
    public Collection<ConstructorStrategyFactory> getConstructorStrategyFactories() {
        return ensureLoaded().getConstructorStrategyFactories();
    }

    @Override
    public void addConstructorStrategyFactory(final ConstructorStrategyFactory factory) {
        ensureLoaded().addConstructorStrategyFactory(factory);
    }

    @Override
    public MutableComponent createTranslation(final String category, final String value, final Object... args) {
        return ensureLoaded().createTranslation(category, value, args);
    }

    @Override
    public ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory() {
        return ensureLoaded().getNetworkComponentMapFactory();
    }

    @Override
    public PlatformRegistry<GridSynchronizer> getGridSynchronizerRegistry() {
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
                                                             final GridServiceFactory gridServiceFactory) {
        return ensureLoaded().createGridInsertionStrategy(containerMenu, player, gridServiceFactory);
    }

    @Override
    public void addGridInsertionStrategyFactory(final GridInsertionStrategyFactory insertionStrategyFactory) {
        ensureLoaded().addGridInsertionStrategyFactory(insertionStrategyFactory);
    }

    @Override
    public GridExtractionStrategy createGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                                               final Player player,
                                                               final GridServiceFactory gridServiceFactory,
                                                               final Storage<ItemResource> itemStorage) {
        return ensureLoaded().createGridExtractionStrategy(containerMenu, player, gridServiceFactory, itemStorage);
    }

    @Override
    public void addGridExtractionStrategyFactory(final GridExtractionStrategyFactory extractionStrategyFactory) {
        ensureLoaded().addGridExtractionStrategyFactory(extractionStrategyFactory);
    }

    @Override
    public GridScrollingStrategy createGridScrollingStrategy(final AbstractContainerMenu containerMenu,
                                                             final Player player,
                                                             final GridServiceFactory gridServiceFactory) {
        return ensureLoaded().createGridScrollingStrategy(containerMenu, player, gridServiceFactory);
    }

    @Override
    public void addGridScrollingStrategyFactory(final GridScrollingStrategyFactory scrollingStrategyFactory) {
        ensureLoaded().addGridScrollingStrategyFactory(scrollingStrategyFactory);
    }

    @Override
    public void addFilteredResourceFactory(final FilteredResourceFactory factory) {
        ensureLoaded().addFilteredResourceFactory(factory);
    }

    @Override
    public FilteredResourceFactory getFilteredResourceFactory() {
        return ensureLoaded().getFilteredResourceFactory();
    }

    @Override
    public void registerIngredientConverter(final IngredientConverter converter) {
        ensureLoaded().registerIngredientConverter(converter);
    }

    @Override
    public IngredientConverter getIngredientConverter() {
        return ensureLoaded().getIngredientConverter();
    }

    private PlatformApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return delegate;
    }
}
