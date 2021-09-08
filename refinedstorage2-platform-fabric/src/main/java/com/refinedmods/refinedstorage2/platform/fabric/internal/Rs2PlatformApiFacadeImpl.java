package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskManagerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.converter.PlatformConverter;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.converter.FluidPlatformConverter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.converter.FluidResourceAmountPlatformConverter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.converter.ItemPlatformConverter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.converter.ItemStackPlatformConverter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.FabricConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.FabricClientStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.FabricStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk.ItemStorageDiskType;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class Rs2PlatformApiFacadeImpl implements Rs2PlatformApiFacade {
    private final PlatformStorageDiskManager clientStorageDiskManager = new FabricClientStorageDiskManager();
    private final ItemPlatformConverter itemPlatformConverter = new ItemPlatformConverter();
    private final ItemStackPlatformConverter itemStackPlatformConverter = new ItemStackPlatformConverter(itemPlatformConverter);
    private final FluidPlatformConverter fluidPlatformConverter = new FluidPlatformConverter();
    private final FluidResourceAmountPlatformConverter fluidResourceAmountPlatformConverter = new FluidResourceAmountPlatformConverter(fluidPlatformConverter);

    @Override
    public PlatformStorageDiskManager getStorageDiskManager(World world) {
        if (world.getServer() == null) {
            return clientStorageDiskManager;
        }

        return world
                .getServer()
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager()
                .getOrCreate(this::createStorageDiskManager, this::createStorageDiskManager, FabricStorageDiskManager.NAME);
    }

    @Override
    public StorageDiskType<Rs2ItemStack> getItemStorageDiskType() {
        return ItemStorageDiskType.INSTANCE;
    }

    @Override
    public ConnectionProvider createConnectionProvider(World world) {
        return new FabricConnectionProvider(world);
    }

    @Override
    public PlatformConverter<Item, Rs2Item> itemConversion() {
        return itemPlatformConverter;
    }

    @Override
    public PlatformConverter<ItemStack, Rs2ItemStack> itemStackConversion() {
        return itemStackPlatformConverter;
    }

    @Override
    public PlatformConverter<Fluid, Rs2Fluid> fluidConversion() {
        return fluidPlatformConverter;
    }

    @Override
    public PlatformConverter<ResourceAmount<FluidVariant>, Rs2FluidStack> fluidResourceAmountConversion() {
        return fluidResourceAmountPlatformConverter;
    }

    @Override
    public TranslatableText createTranslation(String category, String value, Object... args) {
        return Rs2Mod.createTranslation(category, value, args);
    }

    private FabricStorageDiskManager createStorageDiskManager(NbtCompound tag) {
        var manager = createStorageDiskManager();
        manager.read(tag);
        return manager;
    }

    private FabricStorageDiskManager createStorageDiskManager() {
        return new FabricStorageDiskManager(new StorageDiskManagerImpl());
    }
}
