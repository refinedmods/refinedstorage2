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
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReferenceFactory;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReferenceProvider;
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
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface RefinedStorageApi {
    RefinedStorageApi INSTANCE = new RefinedStorageApiProxy();

    PlatformRegistry<StorageType> getStorageTypeRegistry();

    StorageRepository getClientStorageRepository();

    StorageRepository getStorageRepository(Level level);

    StorageContainerItemHelper getStorageContainerItemHelper();

    PlatformRegistry<ResourceType> getResourceTypeRegistry();

    PlatformRegistry<ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry();

    PlatformRegistry<ExporterTransferStrategyFactory> getExporterTransferStrategyRegistry();

    void addExternalStorageProviderFactory(ExternalStorageProviderFactory factory);

    Collection<ExternalStorageProviderFactory> getExternalStorageProviderFactories();

    void addExternalStorageProviderBlockFactory(ExternalStorageProviderFactory factory, Identifier blockId);

    PlatformRegistry<ExternalStorageProviderFactory> getExternalStorageProviderBlocks();

    Collection<DestructorStrategyFactory> getDestructorStrategyFactories();

    void addDestructorStrategyFactory(DestructorStrategyFactory factory);

    Collection<ConstructorStrategyFactory> getConstructorStrategyFactories();

    void addConstructorStrategyFactory(ConstructorStrategyFactory factory);

    void addStorageMonitorExtractionStrategy(StorageMonitorExtractionStrategy strategy);

    StorageMonitorExtractionStrategy getStorageMonitorExtractionStrategy();

    void addStorageMonitorInsertionStrategy(StorageMonitorInsertionStrategy strategy);

    StorageMonitorInsertionStrategy getStorageMonitorInsertionStrategy();

    void addPatternProviderExternalPatternSinkFactory(PatternProviderExternalPatternSinkFactory factory);

    PatternProviderExternalPatternSinkFactory getPatternProviderExternalPatternSinkFactory();

    ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory();

    PlatformRegistry<GridSynchronizer> getGridSynchronizerRegistry();

    ResourceRepositoryMapper<GridResource> getGridResourceRepositoryMapper();

    void addGridResourceRepositoryMapper(Class<? extends ResourceKey> resourceClass,
                                         ResourceRepositoryMapper<GridResource> mapper);

    UpgradeRegistry getUpgradeRegistry();

    NetworkNodeContainerProvider createNetworkNodeContainerProvider();

    InWorldNetworkNodeContainer.Builder createNetworkNodeContainer(BlockEntity blockEntity, NetworkNode networkNode);

    void initializeNetworkNodeContainer(InWorldNetworkNodeContainer container,
                                        @Nullable Level level,
                                        @Nullable Runnable callback);

    void removeNetworkNodeContainer(InWorldNetworkNodeContainer container, @Nullable Level level);

    void updateNetworkNodeContainer(InWorldNetworkNodeContainer container, @Nullable Level level);

    GridInsertionStrategy createGridInsertionStrategy(AbstractContainerMenu containerMenu,
                                                      ServerPlayer player,
                                                      Grid grid);

    void addGridInsertionStrategyFactory(GridInsertionStrategyFactory insertionStrategyFactory);

    void addResourceContainerInsertStrategy(ResourceContainerInsertStrategy strategy);

    Collection<ResourceContainerInsertStrategy> getResourceContainerInsertStrategies();

    GridExtractionStrategy createGridExtractionStrategy(AbstractContainerMenu containerMenu,
                                                        ServerPlayer player,
                                                        Grid grid);

    void addGridExtractionStrategyFactory(GridExtractionStrategyFactory extractionStrategyFactory);

    GridScrollingStrategy createGridScrollingStrategy(AbstractContainerMenu containerMenu,
                                                      ServerPlayer player,
                                                      Grid grid);

    void addGridScrollingStrategyFactory(GridScrollingStrategyFactory scrollingStrategyFactory);

    void addResourceFactory(ResourceFactory factory);

    ResourceFactory getItemResourceFactory();

    StorageType getItemStorageType();

    ResourceFactory getFluidResourceFactory();

    StorageType getFluidStorageType();

    Set<ResourceFactory> getAlternativeResourceFactories();

    void addIngredientConverter(RecipeModIngredientConverter converter);

    RecipeModIngredientConverter getIngredientConverter();

    void addWirelessTransmitterRangeModifier(WirelessTransmitterRangeModifier rangeModifier);

    WirelessTransmitterRangeModifier getWirelessTransmitterRangeModifier();

    Optional<EnergyStorage> getEnergyStorage(ItemStack stack);

    EnergyItemHelper getEnergyItemHelper();

    EnergyStorage asItemEnergyStorage(EnergyStorage energyStorage, ItemStack stack);

    EnergyStorage asBlockItemEnergyStorage(
        EnergyStorage energyStorage,
        ItemStack stack,
        BlockEntityType<?> blockEntityType
    );

    NetworkItemHelper getNetworkItemHelper();

    PlatformRegistry<SlotReferenceFactory> getSlotReferenceFactoryRegistry();

    void addSlotReferenceProvider(SlotReferenceProvider slotReferenceProvider);

    SlotReference createInventorySlotReference(Player player, InteractionHand hand);

    void useSlotReferencedItem(Player player, Item... items);

    PlatformRegistry<PlatformPermission> getPermissionRegistry();

    SecurityPolicy createDefaultSecurityPolicy();

    void sendNoPermissionToOpenMessage(ServerPlayer player, Component target);

    void sendNoPermissionMessage(ServerPlayer player, Component message);

    void sendMessage(ServerPlayer player, Component title, Component message);

    boolean canPlaceNetworkNode(ServerPlayer player, Level level, BlockPos pos, BlockState state);

    Optional<Pattern> getPattern(ItemStack stack, Level level);

    Identifier getCreativeModeTabId();

    Identifier getColoredCreativeModeTabId();

    AbstractNetworkNodeContainerBlockEntity<?> createStorageBlockEntity(BlockPos pos,
                                                                        BlockState state,
                                                                        StorageBlockProvider provider);

    Block createStorageBlock(BlockBehaviour.Properties properties, StorageBlockProvider provider);

    AbstractContainerMenu createStorageBlockContainerMenu(int syncId,
                                                          Player player,
                                                          StorageBlockData data,
                                                          ResourceFactory resourceFactory,
                                                          MenuType<?> menuType);

    StreamCodec<RegistryFriendlyByteBuf, StorageBlockData> getStorageBlockDataStreamCodec();
}
