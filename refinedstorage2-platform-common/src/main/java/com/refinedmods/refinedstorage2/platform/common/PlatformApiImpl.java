package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistryImpl;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkBuilder;
import com.refinedmods.refinedstorage2.api.network.NetworkFactory;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.NoOpGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.internal.network.LevelConnectionProvider;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.ClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.PlatformStorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class PlatformApiImpl implements PlatformApi {
    private static final String ITEM_REGISTRY_KEY = "item";

    private final PlatformStorageRepository clientStorageRepository =
        new ClientStorageRepository(Platform.INSTANCE.getClientToServerCommunications()::sendStorageInfoRequest);
    private final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry =
        new OrderedRegistryImpl<>(createIdentifier(ITEM_REGISTRY_KEY), ItemResourceType.INSTANCE);
    private final ComponentMapFactory<NetworkComponent, Network> networkComponentMapFactory =
        new ComponentMapFactory<>();
    private final NetworkBuilder networkBuilder = new NetworkBuilder(new NetworkFactory(networkComponentMapFactory));
    private final OrderedRegistry<ResourceLocation, StorageType<?>> storageTypeRegistry =
        new OrderedRegistryImpl<>(createIdentifier(ITEM_REGISTRY_KEY), ItemStorageType.INSTANCE);
    private final OrderedRegistry<ResourceLocation, StorageChannelType<?>> storageChannelTypeRegistry =
        new OrderedRegistryImpl<>(createIdentifier(ITEM_REGISTRY_KEY), StorageChannelTypes.ITEM);
    private final OrderedRegistry<ResourceLocation, GridSynchronizer> gridSynchronizerRegistry =
        new OrderedRegistryImpl<>(createIdentifier("off"), new NoOpGridSynchronizer());
    private final OrderedRegistry<ResourceLocation, ImporterTransferStrategyFactory> importerTransferStrategyRegistry =
        new OrderedRegistryImpl<>(createIdentifier("noop"),
            (level, pos, direction) -> (filter, actor, network) -> false);

    @Override
    public OrderedRegistry<ResourceLocation, StorageType<?>> getStorageTypeRegistry() {
        return storageTypeRegistry;
    }

    @Override
    public PlatformStorageRepository getStorageRepository(final Level level) {
        if (level.getServer() == null) {
            return clientStorageRepository;
        }
        final ServerLevel serverLevel = Objects.requireNonNull(level.getServer().getLevel(Level.OVERWORLD));
        return serverLevel
            .getDataStorage()
            .computeIfAbsent(
                this::createStorageRepository,
                this::createStorageRepository,
                PlatformStorageRepositoryImpl.NAME
            );
    }

    private PlatformStorageRepositoryImpl createStorageRepository(final CompoundTag tag) {
        final PlatformStorageRepositoryImpl manager = createStorageRepository();
        manager.read(tag);
        return manager;
    }

    private PlatformStorageRepositoryImpl createStorageRepository() {
        return new PlatformStorageRepositoryImpl(new StorageRepositoryImpl(), storageTypeRegistry);
    }

    @Override
    public OrderedRegistry<ResourceLocation, StorageChannelType<?>> getStorageChannelTypeRegistry() {
        return storageChannelTypeRegistry;
    }

    @Override
    public OrderedRegistry<ResourceLocation, ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry() {
        return importerTransferStrategyRegistry;
    }

    @Override
    public MutableComponent createTranslation(final String category, final String value, final Object... args) {
        return IdentifierUtil.createTranslation(category, value, args);
    }

    @Override
    public OrderedRegistry<ResourceLocation, ResourceType> getResourceTypeRegistry() {
        return resourceTypeRegistry;
    }

    @Override
    public ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory() {
        return networkComponentMapFactory;
    }

    @Override
    public OrderedRegistry<ResourceLocation, GridSynchronizer> getGridSynchronizerRegistry() {
        return gridSynchronizerRegistry;
    }

    @Override
    public void requestNetworkNodeInitialization(final NetworkNodeContainer container,
                                                 final Level level,
                                                 final Runnable callback) {
        final LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        TickHandler.runWhenReady(() -> {
            networkBuilder.initialize(container, connectionProvider);
            callback.run();
        });
    }

    @Override
    public void requestNetworkNodeRemoval(final NetworkNodeContainer container, final Level level) {
        final LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        networkBuilder.remove(container, connectionProvider);
    }
}
