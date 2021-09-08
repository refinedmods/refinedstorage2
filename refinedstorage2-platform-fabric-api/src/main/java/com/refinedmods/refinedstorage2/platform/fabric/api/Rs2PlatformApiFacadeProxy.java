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

public class Rs2PlatformApiFacadeProxy implements Rs2PlatformApiFacade {
    private Rs2PlatformApiFacade facade;

    public void setFacade(Rs2PlatformApiFacade facade) {
        if (this.facade != null) {
            throw new IllegalStateException("Platform API already injected");
        }
        this.facade = facade;
    }

    @Override
    public PlatformStorageDiskManager getStorageDiskManager(World world) {
        return ensureLoaded().getStorageDiskManager(world);
    }

    @Override
    public StorageDiskType<Rs2ItemStack> getItemStorageDiskType() {
        return ensureLoaded().getItemStorageDiskType();
    }

    @Override
    public ConnectionProvider createConnectionProvider(World world) {
        return ensureLoaded().createConnectionProvider(world);
    }

    @Override
    public PlatformConverter<Item, Rs2Item> itemConversion() {
        return ensureLoaded().itemConversion();
    }

    @Override
    public PlatformConverter<ItemStack, Rs2ItemStack> itemStackConversion() {
        return ensureLoaded().itemStackConversion();
    }

    @Override
    public PlatformConverter<Fluid, Rs2Fluid> fluidConversion() {
        return ensureLoaded().fluidConversion();
    }

    @Override
    public PlatformConverter<ResourceAmount<FluidVariant>, Rs2FluidStack> fluidResourceAmountConversion() {
        return ensureLoaded().fluidResourceAmountConversion();
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
