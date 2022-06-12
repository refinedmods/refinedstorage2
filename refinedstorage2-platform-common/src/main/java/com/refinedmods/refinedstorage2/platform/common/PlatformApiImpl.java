package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkBuilder;
import com.refinedmods.refinedstorage2.api.network.NetworkFactory;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.network.LevelConnectionProvider;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.ClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.PlatformStorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class PlatformApiImpl implements PlatformApi {
    private final PlatformStorageRepository clientStorageRepository = new ClientStorageRepository();
    private final ResourceTypeRegistry resourceTypeRegistry = new ResourceTypeRegistry(ItemResourceType.INSTANCE);
    private final ComponentMapFactory<NetworkComponent, Network> networkComponentMapFactory = new ComponentMapFactory<>();
    private final NetworkBuilder networkBuilder = new NetworkBuilder(new NetworkFactory(networkComponentMapFactory));

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
    public StorageType<ItemResource> getItemStorageType() {
        return ItemStorageType.INSTANCE;
    }

    @Override
    public StorageType<FluidResource> getFluidStorageType() {
        return FluidStorageType.INSTANCE;
    }

    @Override
    public TranslatableComponent createTranslation(String category, String value, Object... args) {
        return IdentifierUtil.createTranslation(category, value, args);
    }

    @Override
    public ResourceTypeRegistry getResourceTypeRegistry() {
        return resourceTypeRegistry;
    }

    @Override
    public ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory() {
        return networkComponentMapFactory;
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
        return new PlatformStorageRepositoryImpl(new StorageRepositoryImpl());
    }
}
