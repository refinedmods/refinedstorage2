package com.refinedmods.refinedstorage2.platform.fabric.util;

import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SimpleSingleStackStorage extends SingleStackStorage {
    private ItemStack stack;

    public SimpleSingleStackStorage(final ItemStack stack) {
        this.stack = stack;
    }

    public static SimpleSingleStackStorage forEmptyBucket() {
        return new SimpleSingleStackStorage(new ItemStack(Items.BUCKET));
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    protected void setStack(final ItemStack stack) {
        this.stack = stack;
    }
}
