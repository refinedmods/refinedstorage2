package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.FabricConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.ItemStorageType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class Rs2PlatformApiFacadeImpl implements Rs2PlatformApiFacade {
    private final PlatformStorageRepository clientStorageRepo = new FabricClientStorageRepository();

    @Override
    public PlatformStorageRepository getStorageRepository(Level world) {
        if (world.getServer() == null) {
            return clientStorageRepo;
        }

        return world
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
    public ConnectionProvider createConnectionProvider(Level world) {
        return new FabricConnectionProvider(world);
    }

    @Override
    public TranslatableComponent createTranslation(String category, String value, Object... args) {
        return Rs2Mod.createTranslation(category, value, args);
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
