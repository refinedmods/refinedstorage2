package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;

import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class Rs2PlatformApiFacadeProxy implements Rs2PlatformApiFacade {
    private Rs2PlatformApiFacade facade;

    public void setFacade(Rs2PlatformApiFacade facade) {
        if (this.facade != null) {
            throw new IllegalStateException("Platform API already injected");
        }
        this.facade = facade;
    }

    @Override
    public PlatformStorageManager getStorageDiskManager(World world) {
        return ensureLoaded().getStorageDiskManager(world);
    }

    @Override
    public StorageDiskType<ItemResource> getItemStorageDiskType() {
        return ensureLoaded().getItemStorageDiskType();
    }

    @Override
    public ResourceAmount<ItemResource> toItemResourceAmount(ItemStack stack) {
        return ensureLoaded().toItemResourceAmount(stack);
    }

    @Override
    public ItemStack toItemStack(ResourceAmount<ItemResource> resourceAmount) {
        return ensureLoaded().toItemStack(resourceAmount);
    }

    @Override
    public ConnectionProvider createConnectionProvider(World world) {
        return ensureLoaded().createConnectionProvider(world);
    }

    @Override
    public TranslatableText createTranslation(String category, String value, Object... args) {
        return ensureLoaded().createTranslation(category, value, args);
    }

    private Rs2PlatformApiFacade ensureLoaded() {
        if (facade == null) {
            throw new IllegalStateException("Platform API not loaded yet");
        }
        return facade;
    }
}
