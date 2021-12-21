package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.api.network.NetworkFactory;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistryImpl;
import com.refinedmods.refinedstorage2.api.network.node.NetworkBuilder;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceTypeRegistry;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.LevelConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.internal.resource.filter.ItemResourceType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.ItemStorageType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class Rs2PlatformApiFacadeImpl implements Rs2PlatformApiFacade {
    private final PlatformStorageRepository clientStorageRepository = new FabricClientStorageRepository();
    private final ResourceTypeRegistry resourceTypeRegistry = new ResourceTypeRegistry(ItemResourceType.INSTANCE);
    private final NetworkComponentRegistry networkComponentRegistry = new NetworkComponentRegistryImpl();
    private final NetworkBuilder networkBuilder = new NetworkBuilder(new NetworkFactory(networkComponentRegistry));

    @Override
    public PlatformStorageRepository getStorageRepository(Level level) {
        if (level.getServer() == null) {
            return clientStorageRepository;
        }
        return level
                .getServer()
                .getLevel(Level.OVERWORLD)
                .getDataStorage()
                .computeIfAbsent(this::createStorageRepository, this::createStorageRepository, FabricStorageRepository.NAME);
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
        return Rs2Mod.createTranslation(category, value, args);
    }

    @Override
    public ResourceTypeRegistry getResourceTypeRegistry() {
        return resourceTypeRegistry;
    }

    @Override
    public NetworkComponentRegistry getNetworkComponentRegistry() {
        return networkComponentRegistry;
    }

    @Override
    public void requestNetworkNodeInitialization(NetworkNodeContainer container, Level level) {
        LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        TickHandler.runWhenReady(() -> networkBuilder.initialize(container, connectionProvider));
    }

    @Override
    public void requestNetworkNodeRemoval(NetworkNodeContainer container, Level level) {
        LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        networkBuilder.remove(container, connectionProvider);
    }

    private FabricStorageRepository createStorageRepository(CompoundTag tag) {
        var manager = createStorageRepository();
        manager.read(tag);
        return manager;
    }

    private FabricStorageRepository createStorageRepository() {
        return new FabricStorageRepository(new StorageRepositoryImpl());
    }
}
