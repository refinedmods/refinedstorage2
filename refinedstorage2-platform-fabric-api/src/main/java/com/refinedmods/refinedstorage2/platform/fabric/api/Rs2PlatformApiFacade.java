package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.converter.PlatformConverter;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public interface Rs2PlatformApiFacade {
    Rs2PlatformApiFacade INSTANCE = new Rs2PlatformApiFacadeProxy();

    PlatformStorageDiskManager getStorageDiskManager(World world);

    StorageDiskType<Rs2ItemStack> getItemStorageDiskType();

    ConnectionProvider createConnectionProvider(World world);

    PlatformConverter<Item, Rs2Item> itemConversion();

    PlatformConverter<ItemStack, Rs2ItemStack> itemStackConversion();

    PlatformConverter<Fluid, Rs2Fluid> fluidConversion();

    PlatformConverter<ResourceAmount<FluidVariant>, Rs2FluidStack> fluidResourceAmountConversion();

    TranslatableText createTranslation(String category, String value, Object... args);
}
