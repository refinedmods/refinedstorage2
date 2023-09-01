package com.refinedmods.refinedstorage2.platform.api;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.api.blockentity.constructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.blockentity.destructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.blockentity.wirelesstransmitter.WirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHints;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.item.EnergyItemHelper;
import com.refinedmods.refinedstorage2.platform.api.item.StorageContainerItemHelper;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.api.upgrade.BuiltinUpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
    public StorageContainerItemHelper getStorageContainerItemHelper() {
        return ensureLoaded().getStorageContainerItemHelper();
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
    public void addExternalStorageProviderFactory(final PlatformExternalStorageProviderFactory factory) {
        ensureLoaded().addExternalStorageProviderFactory(factory);
    }

    @Override
    public Collection<PlatformExternalStorageProviderFactory> getExternalStorageProviderFactories() {
        return ensureLoaded().getExternalStorageProviderFactories();
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
    public BuiltinUpgradeDestinations getBuiltinUpgradeDestinations() {
        return ensureLoaded().getBuiltinUpgradeDestinations();
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
                                                             final Grid grid) {
        return ensureLoaded().createGridInsertionStrategy(containerMenu, player, grid);
    }

    @Override
    public void addGridInsertionStrategyFactory(final GridInsertionStrategyFactory insertionStrategyFactory) {
        ensureLoaded().addGridInsertionStrategyFactory(insertionStrategyFactory);
    }

    @Override
    public void addAlternativeGridInsertionHint(final GridInsertionHint hint) {
        ensureLoaded().addAlternativeGridInsertionHint(hint);
    }

    @Override
    public GridInsertionHints getGridInsertionHints() {
        return ensureLoaded().getGridInsertionHints();
    }

    @Override
    public GridExtractionStrategy createGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                                               final Player player,
                                                               final Grid grid) {
        return ensureLoaded().createGridExtractionStrategy(containerMenu, player, grid);
    }

    @Override
    public void addGridExtractionStrategyFactory(final GridExtractionStrategyFactory extractionStrategyFactory) {
        ensureLoaded().addGridExtractionStrategyFactory(extractionStrategyFactory);
    }

    @Override
    public GridScrollingStrategy createGridScrollingStrategy(final AbstractContainerMenu containerMenu,
                                                             final Player player,
                                                             final Grid grid) {
        return ensureLoaded().createGridScrollingStrategy(containerMenu, player, grid);
    }

    @Override
    public void addGridScrollingStrategyFactory(final GridScrollingStrategyFactory scrollingStrategyFactory) {
        ensureLoaded().addGridScrollingStrategyFactory(scrollingStrategyFactory);
    }

    @Override
    public <T> void addResourceFactory(final ResourceFactory<T> factory) {
        ensureLoaded().addResourceFactory(factory);
    }

    @Override
    public ResourceFactory<ItemResource> getItemResourceFactory() {
        return ensureLoaded().getItemResourceFactory();
    }

    @Override
    public PlatformStorageChannelType<ItemResource> getItemStorageChannelType() {
        return ensureLoaded().getItemStorageChannelType();
    }

    @Override
    public StorageType<ItemResource> getItemStorageType() {
        return ensureLoaded().getItemStorageType();
    }

    @Override
    public ResourceFactory<FluidResource> getFluidResourceFactory() {
        return ensureLoaded().getFluidResourceFactory();
    }

    @Override
    public PlatformStorageChannelType<FluidResource> getFluidStorageChannelType() {
        return ensureLoaded().getFluidStorageChannelType();
    }

    @Override
    public StorageType<FluidResource> getFluidStorageType() {
        return ensureLoaded().getFluidStorageType();
    }

    @Override
    public Set<ResourceFactory<?>> getAlternativeResourceFactories() {
        return ensureLoaded().getAlternativeResourceFactories();
    }

    @Override
    public <T> void registerResourceRendering(final Class<T> resourceClass, final ResourceRendering<T> rendering) {
        ensureLoaded().registerResourceRendering(resourceClass, rendering);
    }

    @Override
    public <T> ResourceRendering<T> getResourceRendering(final T resource) {
        return ensureLoaded().getResourceRendering(resource);
    }

    @Override
    public void registerIngredientConverter(final IngredientConverter converter) {
        ensureLoaded().registerIngredientConverter(converter);
    }

    @Override
    public IngredientConverter getIngredientConverter() {
        return ensureLoaded().getIngredientConverter();
    }

    @Override
    public void addWirelessTransmitterRangeModifier(final WirelessTransmitterRangeModifier rangeModifier) {
        ensureLoaded().addWirelessTransmitterRangeModifier(rangeModifier);
    }

    @Override
    public WirelessTransmitterRangeModifier getWirelessTransmitterRangeModifier() {
        return ensureLoaded().getWirelessTransmitterRangeModifier();
    }

    @Override
    public Optional<EnergyStorage> getEnergyStorage(final ItemStack stack) {
        return ensureLoaded().getEnergyStorage(stack);
    }

    @Override
    public EnergyItemHelper getEnergyItemHelper() {
        return ensureLoaded().getEnergyItemHelper();
    }

    @Override
    public EnergyStorage asItemEnergyStorage(final EnergyStorage energyStorage, final ItemStack stack) {
        return ensureLoaded().asItemEnergyStorage(energyStorage, stack);
    }

    private PlatformApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return delegate;
    }
}
