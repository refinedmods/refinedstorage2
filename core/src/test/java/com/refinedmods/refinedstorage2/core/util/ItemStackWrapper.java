package com.refinedmods.refinedstorage2.core.util;

import net.minecraft.item.ItemStack;

import java.util.Objects;

class ItemStackWrapper {
    private final ItemStack stack;

    ItemStackWrapper(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStackWrapper that = (ItemStackWrapper) o;
        return Objects.equals(stack.getItem(), that.stack.getItem())
                && Objects.equals(stack.getTag(), that.stack.getTag())
                && stack.getCount() == that.stack.getCount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack.getItem(), stack.getTag(), stack.getCount());
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}
