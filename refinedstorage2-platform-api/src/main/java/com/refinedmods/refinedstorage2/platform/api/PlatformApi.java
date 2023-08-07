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
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHints;
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
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.Collection;
import java.util.Set;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface PlatformApi {
    PlatformApi INSTANCE = new PlatformApiProxy();

    PlatformRegistry<StorageType<?>> getStorageTypeRegistry();

    StorageRepository getStorageRepository(Level level);

    StorageContainerHelper getStorageContainerHelper();

    PlatformRegistry<PlatformStorageChannelType<?>> getStorageChannelTypeRegistry();

    PlatformRegistry<ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry();

    PlatformRegistry<ExporterTransferStrategyFactory> getExporterTransferStrategyRegistry();

    <T> void addExternalStorageProviderFactory(StorageChannelType<T> channelType,
                                               PlatformExternalStorageProviderFactory factory);

    <T> Collection<PlatformExternalStorageProviderFactory> getExternalStorageProviderFactories(
        StorageChannelType<T> channelType
    );

    Collection<DestructorStrategyFactory> getDestructorStrategyFactories();

    void addDestructorStrategyFactory(DestructorStrategyFactory factory);

    Collection<ConstructorStrategyFactory> getConstructorStrategyFactories();

    void addConstructorStrategyFactory(ConstructorStrategyFactory factory);

    MutableComponent createTranslation(String category, String value, Object... args);

    ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory();

    PlatformRegistry<GridSynchronizer> getGridSynchronizerRegistry();

    UpgradeRegistry getUpgradeRegistry();

    void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level, Runnable callback);

    void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level);

    void requestNetworkNodeUpdate(NetworkNodeContainer container, Level level);

    GridInsertionStrategy createGridInsertionStrategy(AbstractContainerMenu containerMenu,
                                                      Player player,
                                                      GridServiceFactory gridServiceFactory);

    void addGridInsertionStrategyFactory(GridInsertionStrategyFactory insertionStrategyFactory);

    void addAlternativeGridInsertionHint(GridInsertionHint hint);

    GridInsertionHints getGridInsertionHints();

    GridExtractionStrategy createGridExtractionStrategy(AbstractContainerMenu containerMenu,
                                                        Player player,
                                                        GridServiceFactory gridServiceFactory,
                                                        Storage<ItemResource> itemStorage);

    void addGridExtractionStrategyFactory(GridExtractionStrategyFactory extractionStrategyFactory);

    GridScrollingStrategy createGridScrollingStrategy(AbstractContainerMenu containerMenu,
                                                      Player player,
                                                      GridServiceFactory gridServiceFactory);

    void addGridScrollingStrategyFactory(GridScrollingStrategyFactory scrollingStrategyFactory);

    <T> void addResourceFactory(ResourceFactory<T> factory);

    ResourceFactory<ItemResource> getItemResourceFactory();

    ResourceFactory<FluidResource> getFluidResourceFactory();

    Set<ResourceFactory<?>> getAlternativeResourceFactories();

    <T> void registerResourceRendering(Class<T> resourceClass, ResourceRendering<T> rendering);

    <T> ResourceRendering<T> getResourceRendering(T resource);

    void registerIngredientConverter(IngredientConverter converter);

    IngredientConverter getIngredientConverter();
}
