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
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface PlatformApi {
    PlatformApi INSTANCE = new PlatformApiProxy();

    PlatformRegistry<StorageType> getStorageTypeRegistry();

    StorageRepository getClientStorageRepository();

    StorageRepository getStorageRepository(Level level);

    StorageContainerItemHelper getStorageContainerItemHelper();

    PlatformRegistry<ResourceType> getResourceTypeRegistry();

    PlatformRegistry<ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry();

    PlatformRegistry<ExporterTransferStrategyFactory> getExporterTransferStrategyRegistry();

    void addExternalStorageProviderFactory(PlatformExternalStorageProviderFactory factory);

    Collection<PlatformExternalStorageProviderFactory> getExternalStorageProviderFactories();

    Collection<DestructorStrategyFactory> getDestructorStrategyFactories();

    void addDestructorStrategyFactory(DestructorStrategyFactory factory);

    Collection<ConstructorStrategyFactory> getConstructorStrategyFactories();

    void addConstructorStrategyFactory(ConstructorStrategyFactory factory);

    void addStorageMonitorExtractionStrategy(StorageMonitorExtractionStrategy strategy);

    StorageMonitorExtractionStrategy getStorageMonitorExtractionStrategy();

    void addStorageMonitorInsertionStrategy(StorageMonitorInsertionStrategy strategy);

    StorageMonitorInsertionStrategy getStorageMonitorInsertionStrategy();

    ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory();

    PlatformRegistry<GridSynchronizer> getGridSynchronizerRegistry();

    UpgradeRegistry getUpgradeRegistry();

    BuiltinUpgradeDestinations getBuiltinUpgradeDestinations();

    InWorldNetworkNodeContainer createInWorldNetworkNodeContainer(BlockEntity blockEntity,
                                                                  NetworkNode node,
                                                                  String name,
                                                                  int priority,
                                                                  ConnectionLogic connectionLogic,
                                                                  @Nullable Supplier<Object> keyProvider);

    void onNetworkNodeContainerInitialized(InWorldNetworkNodeContainer container,
                                           @Nullable Level level,
                                           @Nullable Runnable callback);

    void onNetworkNodeContainerRemoved(InWorldNetworkNodeContainer container, @Nullable Level level);

    void onNetworkNodeContainerUpdated(InWorldNetworkNodeContainer container, @Nullable Level level);

    GridInsertionStrategy createGridInsertionStrategy(AbstractContainerMenu containerMenu,
                                                      ServerPlayer player,
                                                      Grid grid);

    void addGridInsertionStrategyFactory(GridInsertionStrategyFactory insertionStrategyFactory);

    void addAlternativeGridInsertionHint(GridInsertionHint hint);

    GridInsertionHints getGridInsertionHints();

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

    <T extends ResourceKey> void registerResourceRendering(Class<T> resourceClass, ResourceRendering rendering);

    ResourceRendering getResourceRendering(ResourceKey resource);

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

    NetworkBoundItemHelper getNetworkBoundItemHelper();

    PlatformRegistry<SlotReferenceFactory> getSlotReferenceFactoryRegistry();

    void addSlotReferenceProvider(SlotReferenceProvider slotReferenceProvider);

    SlotReference createInventorySlotReference(Player player, InteractionHand hand);

    void useNetworkBoundItem(Player player, Item... items);

    BuiltinPermissions getBuiltinPermissions();

    PlatformRegistry<PlatformPermission> getPermissionRegistry();

    SecurityPolicy createDefaultSecurityPolicy();

    void sendNoPermissionToOpenMessage(ServerPlayer player, Component target);

    void sendNoPermissionMessage(ServerPlayer player, Component message);

    boolean canPlaceNetworkNode(ServerPlayer player, Level level, BlockPos pos, BlockState state);
}
