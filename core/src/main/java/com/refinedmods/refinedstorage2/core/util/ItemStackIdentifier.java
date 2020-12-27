package com.refinedmods.refinedstorage2.core.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public class ItemStackIdentifier {
    private final Item item;
    private final CompoundTag tag;

    public ItemStackIdentifier(ItemStack stack) {
        this.item = stack.getItem();
        this.tag = stack.getTag();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStackIdentifier that = (ItemStackIdentifier) o;
        return Objects.equals(item, that.item) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag);
    }
}
