package com.refinedmods.refinedstorage.neoforge.support.resource;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;

public class SimpleItemStackResourceHandler extends ItemStackResourceHandler {
    private ItemStack stack;

    private SimpleItemStackResourceHandler(final ItemStack stack) {
        this.stack = stack;
    }

    public static SimpleItemStackResourceHandler forEmptyBucket() {
        return new SimpleItemStackResourceHandler(new ItemStack(Items.BUCKET));
    }

    public static SimpleItemStackResourceHandler forStack(final ItemStack stack) {
        return new SimpleItemStackResourceHandler(stack);
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
