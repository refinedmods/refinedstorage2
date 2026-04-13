package com.refinedmods.refinedstorage.fabric.support.resource;

import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SimpleSingleStackStorage extends SingleStackStorage {
    private ItemStack stack;

    private SimpleSingleStackStorage(final ItemStack stack) {
        this.stack = stack;
    }

    public static SimpleSingleStackStorage forEmptyBucket() {
        return forStack(new ItemStack(Items.BUCKET));
    }

    public static SimpleSingleStackStorage forStack(final ItemStack stack) {
        return new SimpleSingleStackStorage(stack);
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
