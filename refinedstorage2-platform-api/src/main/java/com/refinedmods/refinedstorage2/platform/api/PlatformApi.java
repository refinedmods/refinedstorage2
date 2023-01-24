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

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface PlatformApi {
    PlatformApi INSTANCE = new PlatformApiProxy();

    OrderedRegistry<ResourceLocation, StorageType<?>> getStorageTypeRegistry();

    StorageRepository getStorageRepository(Level level);

    StorageContainerHelper getStorageContainerHelper();

    OrderedRegistry<ResourceLocation, PlatformStorageChannelType<?>> getStorageChannelTypeRegistry();

    OrderedRegistry<ResourceLocation, ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry();

    OrderedRegistry<ResourceLocation, ExporterTransferStrategyFactory> getExporterTransferStrategyRegistry();

    <T> void addExternalStorageProviderFactory(StorageChannelType<T> channelType,
                                               int priority,
                                               PlatformExternalStorageProviderFactory factory);

    <T> Set<PlatformExternalStorageProviderFactory> getExternalStorageProviderFactories(
        StorageChannelType<T> channelType
    );

    MutableComponent createTranslation(String category, String value, Object... args);

    OrderedRegistry<ResourceLocation, ResourceType> getResourceTypeRegistry();

    ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory();

    OrderedRegistry<ResourceLocation, GridSynchronizer> getGridSynchronizerRegistry();

    UpgradeRegistry getUpgradeRegistry();

    void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level, Runnable callback);

    void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level);

    void requestNetworkNodeUpdate(NetworkNodeContainer container, Level level);

    GridInsertionStrategy createGridInsertionStrategy(AbstractContainerMenu containerMenu,
                                                      Player player,
                                                      GridServiceFactory serviceFactory);

    void addGridInsertionStrategyFactory(GridInsertionStrategyFactory insertionStrategyFactory);
}
