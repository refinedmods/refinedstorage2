package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public interface Rs2PlatformApiFacade {
    Rs2PlatformApiFacade INSTANCE = new Rs2PlatformApiFacadeProxy();

    PlatformStorageManager getStorageManager(World world);

    StorageType<ItemResource> getItemBulkStorageType();

    StorageType<FluidResource> getFluidBulkStorageType();

    ResourceAmount<ItemResource> toItemResourceAmount(ItemStack stack);

    ItemStack toItemStack(ResourceAmount<ItemResource> resourceAmount);

    ConnectionProvider createConnectionProvider(World world);

    TranslatableText createTranslation(String category, String value, Object... args);
}
