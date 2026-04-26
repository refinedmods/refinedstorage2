package com.refinedmods.refinedstorage.common.api;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.NetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderExternalPatternSinkFactory;
import com.refinedmods.refinedstorage.common.api.constructordestructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage.common.api.constructordestructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage.common.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategyFactory;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategyFactory;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockData;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockProvider;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItemHelper;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.api.storage.StorageType;
import com.refinedmods.refinedstorage.common.api.storage.externalstorage.ExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.common.api.storagemonitor.StorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.storagemonitor.StorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.common.api.support.resource.RecipeModIngredientConverter;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerInsertStrategy;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReferenceProvider;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage.common.api.wirelesstransmitter.WirelessTransmitterRangeModifier;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class RefinedStorageApiProxy implements RefinedStorageApi {
    @Nullable
    private RefinedStorageApi delegate;

    public void setDelegate(final RefinedStorageApi delegate) {
        if (this.delegate != null) {
            throw new IllegalStateException("API already injected");
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
    public void addExternalStorageProviderFactory(final ExternalStorageProviderFactory factory) {
        ensureLoaded().addExternalStorageProviderFactory(factory);
    }

    @Override
    public Collection<ExternalStorageProviderFactory> getExternalStorageProviderFactories() {
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
    public void addPatternProviderExternalPatternSinkFactory(
        final PatternProviderExternalPatternSinkFactory factory) {
        ensureLoaded().addPatternProviderExternalPatternSinkFactory(factory);
    }

    @Override
    public PatternProviderExternalPatternSinkFactory getPatternProviderExternalPatternSinkFactory() {
        return ensureLoaded().getPatternProviderExternalPatternSinkFactory();
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
    public ResourceRepositoryMapper<GridResource> getGridResourceRepositoryMapper() {
        return ensureLoaded().getGridResourceRepositoryMapper();
    }

    @Override
    public void addGridResourceRepositoryMapper(final Class<? extends ResourceKey> resourceClass,
                                                final ResourceRepositoryMapper<GridResource> mapper) {
        ensureLoaded().addGridResourceRepositoryMapper(resourceClass, mapper);
    }

    @Override
    public UpgradeRegistry getUpgradeRegistry() {
        return ensureLoaded().getUpgradeRegistry();
    }

    @Override
    public NetworkNodeContainerProvider createNetworkNodeContainerProvider() {
        return ensureLoaded().createNetworkNodeContainerProvider();
    }

    @Override
    public InWorldNetworkNodeContainer.Builder createNetworkNodeContainer(final BlockEntity blockEntity,
                                                                          final NetworkNode networkNode) {
        return ensureLoaded().createNetworkNodeContainer(blockEntity, networkNode);
    }

    @Override
    public void initializeNetworkNodeContainer(final InWorldNetworkNodeContainer container,
                                               @Nullable final Level level,
                                               @Nullable final Runnable callback) {
        ensureLoaded().initializeNetworkNodeContainer(container, level, callback);
    }

    @Override
    public void removeNetworkNodeContainer(final InWorldNetworkNodeContainer container,
                                           @Nullable final Level level) {
        ensureLoaded().removeNetworkNodeContainer(container, level);
    }

    @Override
    public void updateNetworkNodeContainer(final InWorldNetworkNodeContainer container,
                                           @Nullable final Level level) {
        ensureLoaded().updateNetworkNodeContainer(container, level);
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
    public void addResourceContainerInsertStrategy(final ResourceContainerInsertStrategy strategy) {
        ensureLoaded().addResourceContainerInsertStrategy(strategy);
    }

    @Override
    public Collection<ResourceContainerInsertStrategy> getResourceContainerInsertStrategies() {
        return ensureLoaded().getResourceContainerInsertStrategies();
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
    public Optional<EnergyStorage> getEnergyStorage(final ItemStack stack, final EnergyItemContext context) {
        return ensureLoaded().getEnergyStorage(stack, context);
    }

    @Override
    public EnergyItemHelper getEnergyItemHelper() {
        return ensureLoaded().getEnergyItemHelper();
    }

    @Override
    public EnergyStorage createItemEnergyStorage(final EnergyStorage energyStorage, final ItemStack stack,
                                                 final EnergyItemContext context) {
        return ensureLoaded().createItemEnergyStorage(energyStorage, stack, context);
    }

    @Override
    public EnergyStorage createBlockItemEnergyStorage(final EnergyStorage energyStorage, final ItemStack stack,
                                                      final BlockEntityType<?> blockEntityType,
                                                      final EnergyItemContext context) {
        return ensureLoaded().createBlockItemEnergyStorage(energyStorage, stack, blockEntityType, context);
    }

    @Override
    public NetworkItemHelper getNetworkItemHelper() {
        return ensureLoaded().getNetworkItemHelper();
    }

    @Override
    public PlatformRegistry<StreamCodec<RegistryFriendlyByteBuf,
        ? extends PlayerSlotReference>> getPlayerSlotReferenceFactories() {
        return ensureLoaded().getPlayerSlotReferenceFactories();
    }

    @Override
    public void addPlayerSlotReferenceProvider(final PlayerSlotReferenceProvider playerSlotReferenceProvider) {
        ensureLoaded().addPlayerSlotReferenceProvider(playerSlotReferenceProvider);
    }

    @Override
    public PlayerSlotReference createPlayerInventorySlotReference(final Player player, final InteractionHand hand) {
        return ensureLoaded().createPlayerInventorySlotReference(player, hand);
    }

    @Override
    public void usePlayerSlotReferencedItem(final Player player, final Item... items) {
        ensureLoaded().usePlayerSlotReferencedItem(player, items);
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
    public void sendMessage(final ServerPlayer player, final Component title, final Component message) {
        ensureLoaded().sendMessage(player, title, message);
    }

    @Override
    public boolean canPlaceNetworkNode(final ServerPlayer player,
                                       final Level level,
                                       final BlockPos pos,
                                       final BlockState state) {
        return ensureLoaded().canPlaceNetworkNode(player, level, pos, state);
    }

    @Override
    public Optional<Pattern> getPattern(final ItemStack stack, final Level level) {
        return ensureLoaded().getPattern(stack, level);
    }

    @Override
    public Identifier getCreativeModeTabId() {
        return ensureLoaded().getCreativeModeTabId();
    }

    @Override
    public Identifier getColoredCreativeModeTabId() {
        return ensureLoaded().getColoredCreativeModeTabId();
    }

    @Override
    public AbstractNetworkNodeContainerBlockEntity<?> createStorageBlockEntity(final BlockPos pos,
                                                                               final BlockState state,
                                                                               final StorageBlockProvider provider) {
        return ensureLoaded().createStorageBlockEntity(pos, state, provider);
    }

    @Override
    public Block createStorageBlock(final BlockBehaviour.Properties properties, final StorageBlockProvider provider) {
        return ensureLoaded().createStorageBlock(properties, provider);
    }

    @Override
    public AbstractContainerMenu createStorageBlockContainerMenu(final int syncId,
                                                                 final Player player,
                                                                 final StorageBlockData data,
                                                                 final ResourceFactory resourceFactory,
                                                                 final MenuType<?> menuType) {
        return ensureLoaded().createStorageBlockContainerMenu(syncId, player, data, resourceFactory, menuType);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StorageBlockData> getStorageBlockDataStreamCodec() {
        return ensureLoaded().getStorageBlockDataStreamCodec();
    }

    @Override
    public boolean isEnergyRequired() {
        return ensureLoaded().isEnergyRequired();
    }

    private RefinedStorageApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("API not loaded yet");
        }
        return delegate;
    }
}
