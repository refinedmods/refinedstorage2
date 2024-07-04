package com.refinedmods.refinedstorage.platform.api;

import com.refinedmods.refinedstorage.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.NetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage.platform.api.grid.GridInsertionHints;
import com.refinedmods.refinedstorage.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.security.BuiltinPermissions;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.api.storage.StorageContainerItemHelper;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage.platform.api.storage.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.platform.api.storagemonitor.StorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.storagemonitor.StorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage.platform.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionLogic;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemHelper;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReferenceFactory;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReferenceProvider;
import com.refinedmods.refinedstorage.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.platform.api.support.resource.RecipeModIngredientConverter;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.platform.api.upgrade.BuiltinUpgradeDestinations;
import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage.platform.api.wirelesstransmitter.WirelessTransmitterRangeModifier;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
    public PlatformRegistry<StorageType> getStorageTypeRegistry() {
        return ensureLoaded().getStorageTypeRegistry();
    }

    @Override
    public StorageRepository getClientStorageRepository() {
        return ensureLoaded().getClientStorageRepository();
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
    public PlatformRegistry<ResourceType> getResourceTypeRegistry() {
        return ensureLoaded().getResourceTypeRegistry();
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
    public void addStorageMonitorExtractionStrategy(final StorageMonitorExtractionStrategy strategy) {
        ensureLoaded().addStorageMonitorExtractionStrategy(strategy);
    }

    @Override
    public StorageMonitorExtractionStrategy getStorageMonitorExtractionStrategy() {
        return ensureLoaded().getStorageMonitorExtractionStrategy();
    }

    @Override
    public void addStorageMonitorInsertionStrategy(final StorageMonitorInsertionStrategy strategy) {
        ensureLoaded().addStorageMonitorInsertionStrategy(strategy);
    }

    @Override
    public StorageMonitorInsertionStrategy getStorageMonitorInsertionStrategy() {
        return ensureLoaded().getStorageMonitorInsertionStrategy();
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
    public InWorldNetworkNodeContainer createInWorldNetworkNodeContainer(
        final BlockEntity blockEntity,
        final NetworkNode node,
        final String name,
        final int priority,
        final ConnectionLogic connectionLogic,
        @Nullable final Supplier<Object> keyProvider
    ) {
        return ensureLoaded().createInWorldNetworkNodeContainer(
            blockEntity,
            node,
            name,
            priority,
            connectionLogic,
            keyProvider
        );
    }

    @Override
    public void onNetworkNodeContainerInitialized(final InWorldNetworkNodeContainer container,
                                                  @Nullable final Level level,
                                                  @Nullable final Runnable callback) {
        ensureLoaded().onNetworkNodeContainerInitialized(container, level, callback);
    }

    @Override
    public void onNetworkNodeContainerRemoved(final InWorldNetworkNodeContainer container,
                                              @Nullable final Level level) {
        ensureLoaded().onNetworkNodeContainerRemoved(container, level);
    }

    @Override
    public void onNetworkNodeContainerUpdated(final InWorldNetworkNodeContainer container,
                                              @Nullable final Level level) {
        ensureLoaded().onNetworkNodeContainerUpdated(container, level);
    }

    @Override
    public GridInsertionStrategy createGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                                             final ServerPlayer player,
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
                                                               final ServerPlayer player,
                                                               final Grid grid) {
        return ensureLoaded().createGridExtractionStrategy(containerMenu, player, grid);
    }

    @Override
    public void addGridExtractionStrategyFactory(final GridExtractionStrategyFactory extractionStrategyFactory) {
        ensureLoaded().addGridExtractionStrategyFactory(extractionStrategyFactory);
    }

    @Override
    public GridScrollingStrategy createGridScrollingStrategy(final AbstractContainerMenu containerMenu,
                                                             final ServerPlayer player,
                                                             final Grid grid) {
        return ensureLoaded().createGridScrollingStrategy(containerMenu, player, grid);
    }

    @Override
    public void addGridScrollingStrategyFactory(final GridScrollingStrategyFactory scrollingStrategyFactory) {
        ensureLoaded().addGridScrollingStrategyFactory(scrollingStrategyFactory);
    }

    @Override
    public void addResourceFactory(final ResourceFactory factory) {
        ensureLoaded().addResourceFactory(factory);
    }

    @Override
    public ResourceFactory getItemResourceFactory() {
        return ensureLoaded().getItemResourceFactory();
    }

    @Override
    public StorageType getItemStorageType() {
        return ensureLoaded().getItemStorageType();
    }

    @Override
    public ResourceFactory getFluidResourceFactory() {
        return ensureLoaded().getFluidResourceFactory();
    }

    @Override
    public StorageType getFluidStorageType() {
        return ensureLoaded().getFluidStorageType();
    }

    @Override
    public Set<ResourceFactory> getAlternativeResourceFactories() {
        return ensureLoaded().getAlternativeResourceFactories();
    }

    @Override
    public <T extends ResourceKey> void registerResourceRendering(final Class<T> resourceClass,
                                                                  final ResourceRendering rendering) {
        ensureLoaded().registerResourceRendering(resourceClass, rendering);
    }

    @Override
    public ResourceRendering getResourceRendering(final ResourceKey resource) {
        return ensureLoaded().getResourceRendering(resource);
    }

    @Override
    public void addIngredientConverter(final RecipeModIngredientConverter converter) {
        ensureLoaded().addIngredientConverter(converter);
    }

    @Override
    public RecipeModIngredientConverter getIngredientConverter() {
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

    @Override
    public EnergyStorage asBlockItemEnergyStorage(final EnergyStorage energyStorage,
                                                  final ItemStack stack,
                                                  final BlockEntityType<?> blockEntityType) {
        return ensureLoaded().asBlockItemEnergyStorage(energyStorage, stack, blockEntityType);
    }

    @Override
    public NetworkBoundItemHelper getNetworkBoundItemHelper() {
        return ensureLoaded().getNetworkBoundItemHelper();
    }

    @Override
    public PlatformRegistry<SlotReferenceFactory> getSlotReferenceFactoryRegistry() {
        return ensureLoaded().getSlotReferenceFactoryRegistry();
    }

    @Override
    public void addSlotReferenceProvider(final SlotReferenceProvider slotReferenceProvider) {
        ensureLoaded().addSlotReferenceProvider(slotReferenceProvider);
    }

    @Override
    public SlotReference createInventorySlotReference(final Player player, final InteractionHand hand) {
        return ensureLoaded().createInventorySlotReference(player, hand);
    }

    @Override
    public void useNetworkBoundItem(final Player player, final Item... items) {
        ensureLoaded().useNetworkBoundItem(player, items);
    }

    @Override
    public BuiltinPermissions getBuiltinPermissions() {
        return ensureLoaded().getBuiltinPermissions();
    }

    @Override
    public PlatformRegistry<PlatformPermission> getPermissionRegistry() {
        return ensureLoaded().getPermissionRegistry();
    }

    @Override
    public SecurityPolicy createDefaultSecurityPolicy() {
        return ensureLoaded().createDefaultSecurityPolicy();
    }

    @Override
    public void sendNoPermissionToOpenMessage(final ServerPlayer player, final Component target) {
        ensureLoaded().sendNoPermissionToOpenMessage(player, target);
    }

    @Override
    public void sendNoPermissionMessage(final ServerPlayer player, final Component message) {
        ensureLoaded().sendNoPermissionMessage(player, message);
    }

    @Override
    public boolean canPlaceNetworkNode(final ServerPlayer player,
                                       final Level level,
                                       final BlockPos pos,
                                       final BlockState state) {
        return ensureLoaded().canPlaceNetworkNode(player, level, pos, state);
    }

    private PlatformApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return delegate;
    }
}
