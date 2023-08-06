package com.refinedmods.refinedstorage2.platform.fabric.util;

import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BucketSingleStackStorage extends SingleStackStorage {
    private ItemStack stack = new ItemStack(Items.BUCKET);

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    protected void setStack(final ItemStack stack) {
        this.stack = stack;
    }
}
