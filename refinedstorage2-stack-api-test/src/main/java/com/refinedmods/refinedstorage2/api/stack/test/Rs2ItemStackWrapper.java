package com.refinedmods.refinedstorage2.api.stack.test;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;

import java.util.Objects;

record Rs2ItemStackWrapper(Rs2ItemStack stack) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rs2ItemStackWrapper that = (Rs2ItemStackWrapper) o;
        return Objects.equals(stack.getItem(), that.stack.getItem())
                && Objects.equals(stack.getTag(), that.stack.getTag())
                && stack.getAmount() == that.stack.getAmount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack.getItem(), stack.getTag(), stack.getAmount());
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}
