package com.refinedmods.refinedstorage2.platform.api;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.constructordestructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.constructordestructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHints;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridExtractionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageContainerItemHelper;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage2.platform.api.storage.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.api.storagemonitor.StorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storagemonitor.StorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.NetworkBoundItemHelper;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReferenceFactory;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReferenceProvider;
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.upgrade.BuiltinUpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.api.wirelesstransmitter.WirelessTransmitterRangeModifier;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface PlatformApi {
    PlatformApi INSTANCE = new PlatformApiProxy();

    PlatformRegistry<StorageType> getStorageTypeRegistry();

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

    MutableComponent createTranslation(String category, String value, Object... args);

    ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory();

    PlatformRegistry<GridSynchronizer> getGridSynchronizerRegistry();

    void writeGridScreenOpeningData(Grid grid, FriendlyByteBuf buf);

    UpgradeRegistry getUpgradeRegistry();

    BuiltinUpgradeDestinations getBuiltinUpgradeDestinations();

    void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level, Runnable callback);

    void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level);

    void requestNetworkNodeUpdate(NetworkNodeContainer container, Level level);

    GridInsertionStrategy createGridInsertionStrategy(AbstractContainerMenu containerMenu,
                                                      Player player,
                                                      Grid grid);

    void addGridInsertionStrategyFactory(GridInsertionStrategyFactory insertionStrategyFactory);

    void addAlternativeGridInsertionHint(GridInsertionHint hint);

    GridInsertionHints getGridInsertionHints();

    GridExtractionStrategy createGridExtractionStrategy(AbstractContainerMenu containerMenu,
                                                        Player player,
                                                        Grid grid);

    void addGridExtractionStrategyFactory(GridExtractionStrategyFactory extractionStrategyFactory);

    GridScrollingStrategy createGridScrollingStrategy(AbstractContainerMenu containerMenu,
                                                      Player player,
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

    void registerIngredientConverter(IngredientConverter converter);

    IngredientConverter getIngredientConverter();

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

    void writeSlotReference(SlotReference slotReference, FriendlyByteBuf buf);

    Optional<SlotReference> getSlotReference(FriendlyByteBuf buf);

    void addSlotReferenceProvider(SlotReferenceProvider slotReferenceProvider);

    SlotReference createInventorySlotReference(Player player, InteractionHand hand);

    void useNetworkBoundItem(Player player, Item... items);
}
