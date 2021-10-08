package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.StorageManagerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.FabricConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricClientStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.ItemStorageDiskType;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class Rs2PlatformApiFacadeImpl implements Rs2PlatformApiFacade {
    private final PlatformStorageManager clientStorageDiskManager = new FabricClientStorageManager();

    @Override
    public PlatformStorageManager getStorageDiskManager(World world) {
        if (world.getServer() == null) {
            return clientStorageDiskManager;
        }

        return world
                .getServer()
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager()
                .getOrCreate(this::createStorageDiskManager, this::createStorageDiskManager, FabricStorageManager.NAME);
    }

    @Override
    public StorageDiskType<ItemResource> getItemStorageDiskType() {
        return ItemStorageDiskType.INSTANCE;
    }

    @Override
    public ResourceAmount<ItemResource> toItemResourceAmount(ItemStack stack) {
        return new ResourceAmount<>(new ItemResource(stack), stack.getCount());
    }

    @Override
    public ItemStack toItemStack(ResourceAmount<ItemResource> resourceAmount) {
        ItemStack stack = new ItemStack(resourceAmount.getResource().getItem(), (int) resourceAmount.getAmount());
        stack.setNbt(resourceAmount.getResource().getTag());
        return stack;
    }

    @Override
    public ConnectionProvider createConnectionProvider(World world) {
        return new FabricConnectionProvider(world);
    }

    @Override
    public TranslatableText createTranslation(String category, String value, Object... args) {
        return Rs2Mod.createTranslation(category, value, args);
    }

    private FabricStorageManager createStorageDiskManager(NbtCompound tag) {
        var manager = createStorageDiskManager();
        manager.read(tag);
        return manager;
    }

    private FabricStorageManager createStorageDiskManager() {
        return new FabricStorageManager(new StorageManagerImpl());
    }
}
