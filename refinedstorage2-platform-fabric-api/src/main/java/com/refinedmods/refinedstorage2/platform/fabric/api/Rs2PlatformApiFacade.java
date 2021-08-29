package com.refinedmods.refinedstorage2.platform.fabric.api;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.item.Item;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public interface Rs2PlatformApiFacade {
    Rs2PlatformApiFacade INSTANCE = new Rs2PlatformApiFacadeProxy();

    PlatformStorageDiskManager getStorageDiskManager(World world);

    StorageDiskType<Rs2ItemStack> getItemStorageDiskType();

    ConnectionProvider createConnectionProvider(World world);

    Rs2Item toRs2Item(Item item);

    Rs2Fluid toRs2Fluid(FluidVariant fluidVariant);

    Item toMcItem(Rs2Item item);

    FluidVariant toMcFluid(Rs2Fluid fluid);

    TranslatableText createTranslation(String category, String value, Object... args);
}