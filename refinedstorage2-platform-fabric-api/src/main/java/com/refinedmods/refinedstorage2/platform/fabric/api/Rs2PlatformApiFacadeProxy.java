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
    public Rs2Item toRs2Item(Item item) {
        return ensureLoaded().toRs2Item(item);
    }

    @Override
    public Rs2Fluid toRs2Fluid(FluidVariant fluidVariant) {
        return ensureLoaded().toRs2Fluid(fluidVariant);
    }

    @Override
    public Item toMcItem(Rs2Item item) {
        return ensureLoaded().toMcItem(item);
    }

    @Override
    public FluidVariant toMcFluid(Rs2Fluid fluid) {
        return ensureLoaded().toMcFluid(fluid);
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
