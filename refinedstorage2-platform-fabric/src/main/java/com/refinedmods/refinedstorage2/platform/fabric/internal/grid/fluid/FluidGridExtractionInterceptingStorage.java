package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid;

import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FluidGridExtractionInterceptingStorage extends SingleStackStorage {
    private ItemStack stack = new ItemStack(Items.BUCKET);

    @Override
    protected ItemStack getStack() {
        return stack;
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.stack = stack;
    }
}
