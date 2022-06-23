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
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.NoOpGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.apiimpl.network.LevelConnectionProvider;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.ClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.PlatformStorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class PlatformApiImpl implements PlatformApi {
    private final PlatformStorageRepository clientStorageRepository = new ClientStorageRepository(Platform.INSTANCE.getClientToServerCommunications()::sendStorageInfoRequest);
    private final OrderedRegistry<ResourceLocation, ResourceType<?>> resourceTypeRegistry = new OrderedRegistryImpl<>(createIdentifier("item"), ItemResourceType.INSTANCE);
    private final ComponentMapFactory<NetworkComponent, Network> networkComponentMapFactory = new ComponentMapFactory<>();
    private final NetworkBuilder networkBuilder = new NetworkBuilder(new NetworkFactory(networkComponentMapFactory));
    private final OrderedRegistry<ResourceLocation, StorageType<?>> storageTypeRegistry = new OrderedRegistryImpl<>(createIdentifier("item"), ItemStorageType.INSTANCE);
    private final OrderedRegistry<ResourceLocation, StorageChannelType<?>> storageChannelTypeRegistry = new OrderedRegistryImpl<>(createIdentifier("item"), StorageChannelTypes.ITEM);
    private final OrderedRegistry<ResourceLocation, GridSynchronizer> gridSynchronizerRegistry = new OrderedRegistryImpl<>(createIdentifier("off"), new NoOpGridSynchronizer());

    @Override
    public OrderedRegistry<ResourceLocation, StorageType<?>> getStorageTypeRegistry() {
        return storageTypeRegistry;
    }

    @Override
    public PlatformStorageRepository getStorageRepository(Level level) {
        if (level.getServer() == null) {
            return clientStorageRepository;
        }
        return level
                .getServer()
                .getLevel(Level.OVERWORLD)
                .getDataStorage()
                .computeIfAbsent(this::createStorageRepository, this::createStorageRepository, PlatformStorageRepositoryImpl.NAME);
    }

    @Override
    public OrderedRegistry<ResourceLocation, StorageChannelType<?>> getStorageChannelTypeRegistry() {
        return storageChannelTypeRegistry;
    }

    @Override
    public StorageType<ItemResource> getItemStorageType() {
        return ItemStorageType.INSTANCE;
    }

    @Override
    public StorageType<FluidResource> getFluidStorageType() {
        return FluidStorageType.INSTANCE;
    }

    @Override
    public MutableComponent createTranslation(String category, String value, Object... args) {
        return IdentifierUtil.createTranslation(category, value, args);
    }

    @Override
    public OrderedRegistry<ResourceLocation, ResourceType<?>> getResourceTypeRegistry() {
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
    public void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level, Runnable callback) {
        LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        TickHandler.runWhenReady(() -> {
            networkBuilder.initialize(container, connectionProvider);
            callback.run();
        });
    }

    @Override
    public void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level) {
        LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        networkBuilder.remove(container, connectionProvider);
    }

    private PlatformStorageRepositoryImpl createStorageRepository(CompoundTag tag) {
        var manager = createStorageRepository();
        manager.read(tag);
        return manager;
    }

    private PlatformStorageRepositoryImpl createStorageRepository() {
        return new PlatformStorageRepositoryImpl(new StorageRepositoryImpl(), storageTypeRegistry);
    }
}
